package expression.evaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SortExpression {

	private String regExString = "\\[(.*?)\\]";
	private Pattern squareBrktPattern = Pattern.compile(regExString);
	private List<String> order = new ArrayList<String>();
	private Map<String,String> exps = new HashMap<String,String>();

	SortExpression(Map<String,String> exps) {
		this.exps = exps;
	}

	public List<String> sort() {
		List<Node> allNodes = new ArrayList<Node>();
		List<Node> finalNodes = new ArrayList<Node>();
		List<String> tempNodeNames = new ArrayList<String>();

		try {
			for (Map.Entry<String,String> entry : this.exps.entrySet()) {

				boolean exists = false;
				Node parentNode = null;
				for(Node node : allNodes) {
					while(node.parent!=null) {
						node=node.parent;
					}
					parentNode = findNode(node, entry.getKey());
					if(parentNode!=null)
						break;
				}

				if(parentNode==null)
					parentNode= new Node(entry.getKey());
				else 
					exists = true;

				for(String child:extractRhsVariablesFromExpression(entry.getValue())) {

					Node childNode = null;
					Node temp = null;

					for(Node node : allNodes) {
						temp=node;
						while(node.parent!=null) {
							node=node.parent;
						}
						childNode = findNode(node, child);
						if(childNode!=null)
							break;
					}
					if(childNode!=null) {
						if(childNode.parent==null) {
							if(exists)
								allNodes.remove(temp);
							childNode.addParent(parentNode);
							parentNode.addChild(childNode);
							exists = true;
						} else if(!childNode.name.contains("_previousvalue") && !tempNodeNames.contains(childNode.name)) {
							tempNodeNames.add(childNode.name);
						}
					} else {
						Node c = new Node(child);
						c.addParent(parentNode);
						parentNode.addChild(c);
					}
				}

				if(!exists) {
					while(parentNode.parent!=null) {
						parentNode=parentNode.parent;
					}
					allNodes.add(parentNode);
				}
			}


			List<String> tempNames = new ArrayList<String>();
			for(Node n:allNodes) {
				while(n.parent!=null) {
					n=n.parent;
				}
				if(!tempNames.contains(n.name)) {
					tempNames.add(n.name);
					finalNodes.add(n);
				}
			}

			for(String str:tempNodeNames) {
				for(Node n:finalNodes) {
					if(sortPriorityNodes(n,str))
						break;
				}
			}

			for(Node n:finalNodes) {
				sortNodes(n);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return order;
	}

	private static Node findNode(Node node, String nodeName) {
		Node result = null;
		try {
			if(node.name.equalsIgnoreCase(nodeName)){
				return node;
			} else {
				for(Node child: node.children) {
					result = findNode(child, nodeName);
					if(result!=null) {
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private boolean sortPriorityNodes(Node node, String priorityNodeName) {
		boolean isDone = false;
		try {
			for(Node child : node.children) {
				if(child.name.equals(priorityNodeName)) {
					child.parent=null;
					sortNodes(child);
					isDone=true;
					break;
				} else 
					sortPriorityNodes(child, priorityNodeName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isDone;
	}

	private void sortNodes(Node node) {
		try {
			for(Node child : node.children) {
				sortNodes(child);
			}
			if(!node.name.contains("_previousvalue") && this.exps.containsKey(node.name) && !order.contains(node.name)) {
				order.add(node.name);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<String> extractRhsVariablesFromExpression(String expression) {
		expression=expression.replace("]{t-", "_previousvalue]{t-");
		Matcher matcher = squareBrktPattern.matcher(expression);
		List<String> result = new ArrayList<String>();

		while (matcher.find()) {
			result.add(matcher.group(1));
		}
		return result;
	}

	class Node {

		String name;
		Node parent;
		List<Node> children = new ArrayList<Node>();

		Node(String name){
			this.name=name;
		}

		public void addParent(Node parent) {
			this.parent=parent;
		}

		public void addChild(Node child) {
			this.children.add(child);
		}
	}

}