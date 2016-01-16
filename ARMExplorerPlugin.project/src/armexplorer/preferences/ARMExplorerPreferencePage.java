package armexplorer.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import armexplorer.Activator;

public class ARMExplorerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	// https://azure.microsoft.com/documentation/articles/resource-group-authenticate-service-principal/
	public ARMExplorerPreferencePage() {
		super(GRID);
		setDescription(
				"Please fill your Microsoft Azure account info. If you aren't sure, please read \"https://azure.microsoft.com/documentation/articles/resource-group-authenticate-service-principal/\".");
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

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
