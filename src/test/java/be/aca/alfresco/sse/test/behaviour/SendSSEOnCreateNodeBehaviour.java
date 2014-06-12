package be.aca.alfresco.sse.test.behaviour;

import be.aca.alfresco.sse.domain.Message;
import be.aca.alfresco.sse.service.ClientEventService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;

public class SendSSEOnCreateNodeBehaviour implements NodeServicePolicies.OnCreateNodePolicy {

	private PolicyComponent policyComponent;
	private ClientEventService clientEventService;
	private NodeService nodeService;

	public void init() {
		JavaBehaviour onCreateNodeBehaviour = new JavaBehaviour(this, NodeServicePolicies.OnCreateNodePolicy.QNAME.getLocalName(), Behaviour.NotificationFrequency.EVERY_EVENT);
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, onCreateNodeBehaviour);
	}

	@Override
	public void onCreateNode(ChildAssociationRef childAssociationRef) {
		Message message = new Message((String) nodeService.getProperty(childAssociationRef.getChildRef(), ContentModel.PROP_NAME));
		clientEventService.sendMessageToUser("admin", message);
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setClientEventService(ClientEventService clientEventService) {
		this.clientEventService = clientEventService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
}
