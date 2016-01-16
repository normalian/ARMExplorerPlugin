package armexplorer.armsdk;

import java.net.URI;

import com.microsoft.azure.utility.AuthHelper;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;

public class AzureConfigFactory {
	// you can check subscriptionid with Get-AzureSubscription after running
	// Add-AzureAccount
	static String subscriptionId = "<input your subscription id>";

	// you can check tenantid with below command
	// you can login with "$creds = Get-Credential # ApplicationId/Password"
	// Add-AzureAccount -Credential $creds -ServicePrincipal -Tenant
	// $subscription.TenantId
	static String tenantId = "<input your tenant id>";

	// you can check clinetid with below command
	// Get-AzureADServicePrincipal -ObjectId <objectId> -Debug
	static String clientId = "<input your client id>";

	// password
	static String clientKey = "<input your password>";

	// static string
	final static String managementUri = "https://management.core.windows.net/";
	final static String armUrl = "https://management.azure.net/";
	final static String armAadUrl = "https://login.windows.net/";

	public static String getTenantId() {
		return tenantId;
	}

	public static void setTenantId(String tenantId) {
		AzureConfigFactory.tenantId = tenantId;
	}

	public static String getClientId() {
		return clientId;
	}

	public static void setClientId(String clientId) {
		AzureConfigFactory.clientId = clientId;
	}

	public static String getClientKey() {
		return clientKey;
	}

	public static void setClientKey(String clientKey) {
		AzureConfigFactory.clientKey = clientKey;
	}

	public static void setSubscriptionId(String subscriptionId) {
		AzureConfigFactory.subscriptionId = subscriptionId;
	}

	public static Configuration createConfiguration() throws Exception {
		return ManagementConfiguration.configure(null, new URI(armUrl), subscriptionId, AuthHelper
				.getAccessTokenFromServicePrincipalCredentials(managementUri, armAadUrl, tenantId, clientId, clientKey)
				.getAccessToken());
	}

	public static String getSubscriptionId() {
		return subscriptionId;
	}
}
