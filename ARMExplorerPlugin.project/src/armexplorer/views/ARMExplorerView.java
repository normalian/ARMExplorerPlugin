package armexplorer.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import com.microsoft.azure.management.resources.models.GenericResourceExtended;

import armexplorer.Activator;
import armexplorer.armsdk.AzureConfigFactory;
import armexplorer.armsdk.AzureResourceFactory;
import armexplorer.preferences.ARMExplorerPreferenceConstants;

public class ARMExplorerView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "armexplorer.views.ARMExplorerView";

	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;

	class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		private TreeParent invisibleRoot;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot == null)
					initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}

		public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject) child).getParent();
			}
			return null;
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent) parent).getChildren();
			}
			return new Object[0];
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent) parent).hasChildren();
			return false;
		}

		private void initialize() {
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();

			// read stored account info
			{
				String subscriptionId = store.getString(ARMExplorerPreferenceConstants.SUBSCRIPTION_ID);
				String tenantId = store.getString(ARMExplorerPreferenceConstants.TENANT_ID);
				String clientId = store.getString(ARMExplorerPreferenceConstants.CLIENT_ID);
				String clientKey = store.getString(ARMExplorerPreferenceConstants.CLIENT_KEY);

				ILog log = Activator.getDefault().getLog();
				log.log(new Status(IStatus.INFO, Activator.PLUGIN_ID,
						String.format("load info subscriptionId=[%s], tenantId=[%s], clientId=[%s], clientKey=[%s]",
								subscriptionId, tenantId, clientId, clientKey)));

				AzureConfigFactory.setSubscriptionId(subscriptionId);
				AzureConfigFactory.setTenantId(tenantId);
				AzureConfigFactory.setClientId(clientId);
				AzureConfigFactory.setClientKey(clientKey);
			}

			updateView();
		}

		private void updateView() {
			// output log
			ILog log = Activator.getDefault().getLog();

			// root node
			TreeParent root = null;
			if (StringUtils.isBlank(AzureConfigFactory.getSubscriptionId()) == false
					&& StringUtils.isBlank(AzureConfigFactory.getTenantId()) == false
					&& StringUtils.isBlank(AzureConfigFactory.getClientId()) == false
					&& StringUtils.isBlank(AzureConfigFactory.getClientKey()) == false) {
				HashMap<String, List<GenericResourceExtended>> greListMap = null;
				try {
					greListMap = AzureResourceFactory.getResourcesAsMap();
					// create subscriptionId node
					root = new TreeParent(AzureConfigFactory.getSubscriptionId());

					for (Entry<String, List<GenericResourceExtended>> entry : greListMap.entrySet()) {
						TreeParent p = new TreeParent(entry.getKey());
						for (GenericResourceExtended gre : entry.getValue()) {
							TreeObject to1 = new TreeObject(gre.getName());
							p.addChild(to1);
						}
						root.addChild(p);
					}
				} catch (Exception e) {
					// output log
					log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "ARM SDK Error", e));
					e.printStackTrace();

					// when subscription id is broken
					root = new TreeParent("Subscription Info is broken");
				}

			} else {
				// when subscription id is empty
				root = new TreeParent("Subscription Info isn't filled");
			}

			this.invisibleRoot = new TreeParent("");
			this.invisibleRoot.addChild(root);
		}
	}

	public ARMExplorerView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "ARMExplorerPlugin.viewer");
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				ILog log = Activator.getDefault().getLog();
				log.log(new Status(IStatus.INFO, Activator.PLUGIN_ID,
						String.format("property changing: source=[%s], property=[%s] new value=[%s]", event.getSource(),
								event.getProperty(), event.getNewValue())));

				// update property's value
				if (ARMExplorerPreferenceConstants.SUBSCRIPTION_ID.equals(event.getProperty())) {
					AzureConfigFactory.setSubscriptionId(event.getNewValue().toString());
				} else if (ARMExplorerPreferenceConstants.TENANT_ID.equals(event.getProperty())) {
					AzureConfigFactory.setTenantId(event.getNewValue().toString());
				} else if (ARMExplorerPreferenceConstants.CLIENT_ID.equals(event.getProperty())) {
					AzureConfigFactory.setClientId(event.getNewValue().toString());
				} else if (ARMExplorerPreferenceConstants.CLIENT_KEY.equals(event.getProperty())) {
					AzureConfigFactory.setClientKey(event.getNewValue().toString());
				} else {
					return;
				}

				// This line is used for updating view
				viewer.setInput(getSite());
			}
		});

	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ARMExplorerView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		// IActionBars bars = getViewSite().getActionBars();

		// TODO : implement LocalPullDown
		// fillLocalPullDown(bars.getMenuManager());

		// TODO : implement localtoolbar
		// fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();

				// if double clicked object isn't node, end this process
				String message = "";
				if (obj instanceof TreeParent) {
					return;
				} else if (obj instanceof TreeObject) {
					TreeObject treeObject = (TreeObject) obj;
					if (treeObject.getParent().getName().contains("Classic")) {
						message = String.format("%s is classic type resource called as %s", treeObject.getName(),
								treeObject.getParent().getName());
					} else {
						// TODO: showing resource type info
						message = "Now under development showing resource info.";
					}
				}
				showMessage(message);
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(), "ARM Explorer", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
