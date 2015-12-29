package moskitt4me.projectmanager.core.providers;

import java.util.ArrayList;
import java.util.List;

import moskitt4me.projectmanager.methodspecification.MethodElements;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.epf.uma.Activity;
import org.eclipse.epf.uma.BreakdownElement;
import org.eclipse.epf.uma.CapabilityPattern;
import org.eclipse.epf.uma.DeliveryProcess;
import org.eclipse.epf.uma.MethodElement;
import org.eclipse.epf.uma.ProcessComponent;
import org.eclipse.epf.uma.ProcessElement;
import org.eclipse.epf.uma.ProcessPackage;
import org.eclipse.epf.uma.TaskDescriptor;
import org.eclipse.epf.uma.VariabilityElement;
import org.eclipse.epf.uma.VariabilityType;

/*
 * This class is in charge of providing content to the Process view
 */
public class ProcessContentProvider extends AdapterFactoryContentProvider {
	
	/*
	 * This collection is needed to avoid creating objects contained
	 * in capability patterns more than once. It fixes a problem with
	 * the refresh action.
	 */
	private List<MethodElement> capabilityPatternObjects;
	
	public ProcessContentProvider(AdapterFactory adapterFactory) {
		
		super(adapterFactory);
		
		capabilityPatternObjects = new ArrayList<MethodElement>();
	}
	
	@Override
	public Object[] getElements(Object object) {
		
		if(object instanceof IProject) {
			Object[] elements = new Object[1];
			elements[0] = MethodElements.loadedDeliveryProcess;
			
			return elements;
		}
		
		return super.getElements(object);
	}
	
	@Override
	public boolean hasChildren(Object object) {
		
		if (object instanceof DeliveryProcess ||
				object instanceof CapabilityPattern ||
				object instanceof Activity) {
			
			return true;
		}
		else if (object instanceof TaskDescriptor) {
			
			return false;
		}
		
		return super.hasChildren(object);
	}
	
	@Override
	public Object[] getChildren(Object object) {
		
		if(object instanceof DeliveryProcess) {
			DeliveryProcess dp = (DeliveryProcess) object;

			List<Object> children = new ArrayList<Object>();
			
			EObject container = dp.eContainer();
			if(container instanceof ProcessComponent) {
				ProcessComponent pc = (ProcessComponent) container;
				
				for(EObject eobj : pc.eContents()) {
					if(eobj instanceof ProcessPackage) {
						ProcessPackage pp = (ProcessPackage) eobj;
						
						for(ProcessElement pe : pp.getProcessElements()) {
							if(pe instanceof Activity) {
								children.add((Activity) pe);
							}
						}
					}
					else if(eobj instanceof TaskDescriptor) {
						children.add((TaskDescriptor) eobj);
					}
				}
			}
			
			return children.toArray();
		}
		else if(object instanceof CapabilityPattern) {
			CapabilityPattern cp = (CapabilityPattern) object;
			
			if(cp.getVariabilityType().equals(VariabilityType.EXTENDS)) {
				CapabilityPattern cp2 = MethodElements.getCapabilityPattern(getGUId(cp));
				
				if(cp2 != null) {
					List<String> guids = new ArrayList<String>();
					if(cp instanceof CapabilityPatternCP) {
						CapabilityPatternCP cpcp = (CapabilityPatternCP) cp;
						guids.addAll(cpcp.getCpGuids());
						guids.add(cp.getGuid());
					}
					else {
						guids.add(cp.getGuid());
					}
					return getActivityChildren(cp2, guids, cp.getName()).toArray();
				}
				else {
					return getActivityChildren(cp, null, null).toArray();
				}
			}
			else {
				return getActivityChildren(cp, null, null).toArray();
			}
			
		}
		else if(object instanceof Activity) {
			Activity actv = (Activity) object;
			if(actv instanceof ActivityCP) {
				ActivityCP actvcp = (ActivityCP) actv;
				
				return getActivityChildren(actvcp, actvcp.getCpGuids(), actvcp.getCpName()).toArray();
			}
			else {
				return getActivityChildren(actv, null, null).toArray();
			}
		}
		
		return new Object[]{};
	}
	
	private List<Object> getActivityChildren(Activity actv, List<String> cpguids, String cpname) {
		
		List<Object> children = new ArrayList<Object>();
		
		List<BreakdownElement> elements = actv.getBreakdownElements();
		int size = elements.size();
		
		for(int i = 0; i < size; i++) {
			
			BreakdownElement be = elements.get(i);
			
			if(be instanceof CapabilityPattern) {
				CapabilityPattern cp = (CapabilityPattern) be;
				
				if(cpguids != null && cpguids.size() > 0) {
					MethodElement me = getMethodElement(cp, cpguids);
					if(me != null && me instanceof CapabilityPatternCP) {
						CapabilityPatternCP cpcp = (CapabilityPatternCP) me;
						if(!contains(cpcp, children, cpguids)) {
							children.add(cpcp);
						}
					}
					else {
						CapabilityPatternCP cpcp = new CapabilityPatternCP(cpguids, cpname, cp);
						if(me == null) {
							this.capabilityPatternObjects.add(cpcp);
						}
						if(!contains(cpcp, children, cpguids)) {
							children.add(cpcp);
						}
					}
				}
				else {
					if(!contains(cp, children, cpguids)) {
						children.add(cp);
					}
				}
			}
			else if(be instanceof Activity) {
				Activity actv2 = (Activity) be;
				
				if(cpguids != null && cpguids.size() > 0) {
					MethodElement me = getMethodElement(actv2, cpguids);
					if(me != null && me instanceof ActivityCP) {
						ActivityCP actvcp = (ActivityCP) me;
						if(!contains(actvcp, children, cpguids)) {
							children.add(actvcp);
						}
					}
					else {
						ActivityCP actvcp = new ActivityCP(cpguids, cpname, actv2);
						if(me == null) {
							this.capabilityPatternObjects.add(actvcp);
						}
						if(!contains(actvcp, children, cpguids)) {
							children.add(actvcp);
						}
					}
				}
				else {
					if(!contains(actv2, children, cpguids)) {
						children.add(actv2);
					}
				}
			}
			else if(be instanceof TaskDescriptor) {
				TaskDescriptor td = (TaskDescriptor) be;
				
				if(cpguids != null && cpguids.size() > 0) {
					MethodElement me = getMethodElement(td, cpguids);
					if(me != null && me instanceof TaskDescriptorCP) {
						TaskDescriptorCP tdcp = (TaskDescriptorCP) me;
						if(!contains(tdcp, children, cpguids)) {
							children.add(tdcp);
						}
					}
					else {
						TaskDescriptorCP tdcp = new TaskDescriptorCP(cpguids, cpname, td);
						if(me == null) {
							this.capabilityPatternObjects.add(tdcp);
						}
						if(!contains(tdcp, children, cpguids)) {
							children.add(tdcp);
						}
					}
				}
				else {
					if(!contains(td, children, cpguids)) {
						children.add(td);
					}
				}
			}
		}
		
		return children;
	}
	
	private String getGUId(CapabilityPattern cp) {
		
		VariabilityElement ve = cp.getVariabilityBasedOnElement();
		if(ve instanceof CapabilityPattern) {
			CapabilityPattern cp2 = (CapabilityPattern) ve;
			if(cp2.eIsProxy()) {
				URI proxyURI = ((InternalEObject)cp2).eProxyURI();
				return proxyURI.host();
			}
		}
		
		return "";
	}
	
	private boolean contains(MethodElement elem, List<Object> elements, List<String> cpIds) {
		
		if(elements == null) {
			elements = new ArrayList<Object>();
		}
		if(cpIds == null) {
			cpIds = new ArrayList<String>();
		}
		
		for(Object obj : elements) {
			if(obj instanceof MethodElement) {
				MethodElement me = (MethodElement) obj;
				if(me.getGuid().equals(elem.getGuid())) {
					List<String> guids = new ArrayList<String>();
					if(me instanceof CapabilityPatternCP) {
						CapabilityPatternCP cpcp = (CapabilityPatternCP) me;
						if(elem instanceof CapabilityPatternCP) {
							CapabilityPatternCP cpcp2 = (CapabilityPatternCP) elem;
							guids.addAll(cpcp2.getCpGuids());
						}
						else {
							guids.addAll(cpIds);
						}
						if(cpcp.getCpGuids().size() == guids.size()) {
							boolean equals = true;
							for(int i = 0; i < cpcp.getCpGuids().size() && equals; i++) {
								if(!cpcp.getCpGuids().get(i).equals(guids.get(i))) {
									equals = false;
								}
							}
							if(equals) {
								return true;
							}
						}
					}
					else if(me instanceof ActivityCP) {
						ActivityCP actvcp = (ActivityCP) me;
						if(elem instanceof ActivityCP) {
							ActivityCP actvcp2 = (ActivityCP) elem;
							guids.addAll(actvcp2.getCpGuids());
						}
						else {
							guids.addAll(cpIds);
						}
						if(actvcp.getCpGuids().size() == guids.size()) {
							boolean equals = true;
							for(int i = 0; i < actvcp.getCpGuids().size() && equals; i++) {
								if(!actvcp.getCpGuids().get(i).equals(guids.get(i))) {
									equals = false;
								}
							}
							if(equals) {
								return true;
							}
						}
					}
					else if(me instanceof TaskDescriptorCP) {
						TaskDescriptorCP tdcp = (TaskDescriptorCP) me;
						if(elem instanceof TaskDescriptorCP) {
							TaskDescriptorCP tdcp2 = (TaskDescriptorCP) elem;
							guids.addAll(tdcp2.getCpGuids());
						}
						else {
							guids.addAll(cpIds);
						}
						if(tdcp.getCpGuids().size() == guids.size()) {
							boolean equals = true;
							for(int i = 0; i < tdcp.getCpGuids().size() && equals; i++) {
								if(!tdcp.getCpGuids().get(i).equals(guids.get(i))) {
									equals = false;
								}
							}
							if(equals) {
								return true;
							}
						}
					}
				}
			}
		}
		
		return false;
	}
	
	private MethodElement getMethodElement(MethodElement elem, List<String> cpIds) {
		
		if(cpIds == null) {
			cpIds = new ArrayList<String>();
		}
		
		for(MethodElement me : capabilityPatternObjects) {
			if(me.getGuid().equals(elem.getGuid())) {
				List<String> guids = new ArrayList<String>();
				if(me instanceof CapabilityPatternCP) {
					CapabilityPatternCP cpcp = (CapabilityPatternCP) me;
					if(elem instanceof CapabilityPatternCP) {
						CapabilityPatternCP cpcp2 = (CapabilityPatternCP) elem;
						guids.addAll(cpcp2.getCpGuids());
					}
					else {
						guids.addAll(cpIds);
					}
					if(cpcp.getCpGuids().size() == guids.size()) {
						boolean equals = true;
						for(int i = 0; i < cpcp.getCpGuids().size() && equals; i++) {
							if(!cpcp.getCpGuids().get(i).equals(guids.get(i))) {
								equals = false;
							}
						}
						if(equals) {
							return me;
						}
					}
				}
				else if(me instanceof ActivityCP) {
					ActivityCP actvcp = (ActivityCP) me;
					if(elem instanceof ActivityCP) {
						ActivityCP actvcp2 = (ActivityCP) elem;
						guids.addAll(actvcp2.getCpGuids());
					}
					else {
						guids.addAll(cpIds);
					}
					if(actvcp.getCpGuids().size() == guids.size()) {
						boolean equals = true;
						for(int i = 0; i < actvcp.getCpGuids().size() && equals; i++) {
							if(!actvcp.getCpGuids().get(i).equals(guids.get(i))) {
								equals = false;
							}
						}
						if(equals) {
							return me;
						}
					}
				}
				else if(me instanceof TaskDescriptorCP) {
					TaskDescriptorCP tdcp = (TaskDescriptorCP) me;
					if(elem instanceof TaskDescriptorCP) {
						TaskDescriptorCP tdcp2 = (TaskDescriptorCP) elem;
						guids.addAll(tdcp2.getCpGuids());
					}
					else {
						guids.addAll(cpIds);
					}
					if(tdcp.getCpGuids().size() == guids.size()) {
						boolean equals = true;
						for(int i = 0; i < tdcp.getCpGuids().size() && equals; i++) {
							if(!tdcp.getCpGuids().get(i).equals(guids.get(i))) {
								equals = false;
							}
						}
						if(equals) {
							return me;
						}
					}
				}
			}
		}
		
		return null;
	}
	
	public void clear() {
		
		if(this.capabilityPatternObjects != null) {
			this.capabilityPatternObjects.clear();
		}
	}
}
