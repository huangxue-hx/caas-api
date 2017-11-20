package com.harmonycloud.dao.user.bean;

public enum InitClusterRoleEnum {


    admin("admin", "{\"name\":\"admin\",\"index\":\"90\",\"resource\":[{\"name\":\"*\",\"operations\":[\"*\"]}]}"),
    tm("tm", "{\"name\":\"tm\",\"index\":\"80\",\"resource\":[{\"name\":\"bindings\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"componentstatuses\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"configmaps\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"endpoints\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"events\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"limitranges\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"namespaces\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"namespaces/finalize\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"namespaces/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"nodes\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"nodes/proxy\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"nodes/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"persistentvolumeclaims\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"persistentvolumeclaims/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"persistentvolumes\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"persistentvolumes/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods/attach\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods/binding\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods/exec\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods/log\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods/portforward\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods/proxy\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"podtemplates\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"replicationcontrollers\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"replicationcontrollers/scale\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"replicationcontrollers/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"resourcequotas\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"resourcequotas/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"secrets\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"serviceaccounts\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"services\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"services/proxy\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"services/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"daemonsets\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"daemonsets/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"deployments\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"deployments/rollback\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"deployments/scale\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"deployments/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"horizontalpodautoscalers\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"horizontalpodautoscalers/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"ingresses\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"ingresses/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"jobs\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"jobs/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"networkpolicies\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"replicasets\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"replicasets/scale\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"replicasets/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"thirdpartyresources\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"image\",\"operations\":[\"read\",\"write\"]}]}"),
    dev("dev", "{\"name\":\"dev\",\"index\":\"60\",\"resource\":[{\"name\":\"configmaps\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"bindings\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"endpoints\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"events\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"persistentvolumeclaims\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"persistentvolumeclaims/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods/attach\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods/binding\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods/exec\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods/log\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods/portforward\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods/proxy\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"podtemplates\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"replicationcontrollers\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"replicationcontrollers/scale\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"replicationcontrollers/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"secrets\",\"operations\":[\"create\"]},{\"name\":\"services\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"services/proxy\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"services/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"deployments\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"deployments/rollback\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"deployments/scale\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"deployments/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"horizontalpodautoscalers\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"horizontalpodautoscalers/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"ingresses\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"ingresses/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"replicasets\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"replicasets/scale\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"replicasets/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"thirdpartyresources\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"image\",\"operations\":[\"read\",\"write\"]}]}"),
    //nonResource("nonResource","{\"name\":\"nonResource\",\"index\":\"1000\",\"noneResource\":[{\"name\":\"*\",\"operations\":[\"*\"]}]}");
//    tester("tester","{\"name\":\"tester\",\"index\":\"50\",\"resource\":[{\"name\":\"configmaps\",\"operations\":[\"get\",\"list\",\"watch\",\"proxy\",\"redirect\"]},{\"name\":\"bindings\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"endpoints\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"events\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"persistentvolumeclaims\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"persistentvolumeclaims/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods/attach\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods/binding\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods/exec\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods/log\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods/portforward\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods/proxy\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"pods/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"podtemplates\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"replicationcontrollers\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"replicationcontrollers/scale\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"replicationcontrollers/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"secrets\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"services\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"services/proxy\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"services/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"deployments\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"deployments/rollback\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"deployments/scale\",\"operations\":[\"get\",\"list\",\"patch\",\"watch\",\"proxy\",\"redirect\"]},{\"name\":\"deployments/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"horizontalpodautoscalers\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"horizontalpodautoscalers/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"ingresses\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"ingresses/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"replicasets\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"replicasets/scale\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"replicasets/status\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"thirdpartyresources\",\"operations\":[\"get\",\"list\",\"create\",\"update\",\"patch\",\"watch\",\"proxy\",\"redirect\",\"delete\",\"deletecollection\"]},{\"name\":\"image\",\"operations\":[\"read\",\"write\"]}]}"),
//    harbor_project_developer("harbor_project_developer","{\"name\":\"harbor_project_developer\",\"index\":\"1000\",\"resource\":[{\"name\":\"image\",\"operations\":[\"write\"]}]}"),
//    harbor_project_watcher("harbor_project_watcher","{\"name\":\"harbor_project_watcher\",\"index\":\"1000\",\"resource\":[{\"name\":\"image\",\"operations\":[\"read\"]}]}"
    defaultRole("defaultRole",
            "{" +
                    "\"name\": \"defaultRole\"," +
                    "			\"index\": \"80\"," +
                    "\"resource\": [" +
                    "{" +
                    "\"name\": \"bindings\"," +
                    "\"operations\": [" +
                    "					\"get\"," +
                    "					\"list\"," +
                    "					\"create\"," +
                    "					\"update\"," +
                    "					\"patch\"," +
                    "					\"watch\"," +
                    "					\"proxy\"," +
                    "					\"redirect\"," +
                    "					\"delete\"," +
                    "					\"deletecollection\"" +
                    "					]" +
                    "			}," +
                    "	{" +
                    "            \"name\": \"componentstatuses\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"configmaps\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"endpoints\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"events\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"limitranges\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"namespaces\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"namespaces/finalize\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"namespaces/status\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"nodes\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"nodes/proxy\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"nodes/status\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"persistentvolumeclaims\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"persistentvolumeclaims/status\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"persistentvolumes\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"persistentvolumes/status\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"pods\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"pods/attach\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"pods/binding\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"pods/exec\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"pods/log\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"pods/portforward\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"pods/proxy\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"pods/status\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"podtemplates\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"replicationcontrollers\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"replicationcontrollers/scale\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"replicationcontrollers/status\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"resourcequotas\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"resourcequotas/status\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"secrets\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"serviceaccounts\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"services\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"services/proxy\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"services/status\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"daemonsets\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"daemonsets/status\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"deployments\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"deployments/rollback\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"deployments/scale\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"deployments/status\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"horizontalpodautoscalers\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"horizontalpodautoscalers/status\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"ingresses\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"ingresses/status\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"jobs\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"jobs/status\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"networkpolicies\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"replicasets\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"replicasets/scale\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"replicasets/status\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"thirdpartyresources\"," +
                    "            \"operations\": [" +
                    "                \"get\"," +
                    "                \"list\"," +
                    "                \"create\"," +
                    "                \"update\"," +
                    "                \"patch\"," +
                    "                \"watch\"," +
                    "                \"proxy\"," +
                    "                \"redirect\"," +
                    "                \"delete\"," +
                    "                \"deletecollection\"" +
                    "            ]" +
                    "	}," +
                    "	{" +
                    "            \"name\": \"image\"," +
                    "            \"operations\": [" +
                    "                \"read\"," +
                    "                \"write\"" +
                    "            ]" +
                    "	}" +
                    "    ]" +
                    "	}");
    private String name;
    private String json;

    private InitClusterRoleEnum(String name, String json) {
        this.name = name;
        this.json = json;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }


}
