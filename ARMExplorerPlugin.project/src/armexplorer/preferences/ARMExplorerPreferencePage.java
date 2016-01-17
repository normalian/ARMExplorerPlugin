package armexplorer.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import armexplorer.Activator;

public class ARMExplorerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public ARMExplorerPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		Link link = new Link(parent, SWT.NONE);
		link.setText(ARMExplorerPreferenceConstants.PREFERENCE_ACCOUNT_INFO_MESSAGE);
		link.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// Open Azure Account info article with default Browser. 
				Program.launch(event.text);
			}
		});
		return super.createContents(parent);
	};

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {
		addField(new StringFieldEditor(ARMExplorerPreferenceConstants.SUBSCRIPTION_ID, "Subscription ID",
				getFieldEditorParent()));
		addField(new StringFieldEditor(ARMExplorerPreferenceConstants.TENANT_ID, "Tenant ID", getFieldEditorParent()));
		addField(new StringFieldEditor(ARMExplorerPreferenceConstants.CLIENT_ID, "Client ID", getFieldEditorParent()));
		addField(
				new StringFieldEditor(ARMExplorerPreferenceConstants.CLIENT_KEY, "Client Key", getFieldEditorParent()));
	}

}
