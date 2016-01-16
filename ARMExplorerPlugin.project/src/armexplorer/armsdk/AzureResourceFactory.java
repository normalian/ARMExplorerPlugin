package armexplorer.armsdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementService;
import com.microsoft.azure.management.resources.models.GenericResourceExtended;
import com.microsoft.windowsazure.Configuration;

public class AzureResourceFactory {
	public static GenericResourceExtended getResource() throws Exception {
		Configuration config = AzureConfigFactory.createConfiguration();
		ResourceManagementClient resourceManagementClient = ResourceManagementService.create(config);

		return resourceManagementClient.getResourcesOperations().list(null).getResources().get(0);
	}

	public static List<GenericResourceExtended> getResources() throws Exception {
		Configuration config = AzureConfigFactory.createConfiguration();
		ResourceManagementClient resourceManagementClient = ResourceManagementService.create(config);

		return resourceManagementClient.getResourcesOperations().list(null).getResources();
	}

	public static HashMap<String, List<GenericResourceExtended>> getResourcesAsMap() throws Exception {
		List<GenericResourceExtended> greList = getResources();

		HashMap<String, List<GenericResourceExtended>> greListMap = new HashMap<>();
		for (GenericResourceExtended gre : greList) {
			if (greListMap.containsKey(gre.getType()) == true) {
				greListMap.get(gre.getType()).add(gre);
			} else {
				List<GenericResourceExtended> list = new ArrayList<>();
				list.add(gre);
				greListMap.put(gre.getType(), list);
			}
		}

		return greListMap;
	}
}
