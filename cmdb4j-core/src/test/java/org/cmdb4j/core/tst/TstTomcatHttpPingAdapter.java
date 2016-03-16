package org.cmdb4j.core.tst;

import org.cmdb4j.core.model.Resource;

import fr.an.dynadapter.alt.IAdapterAlternativeFactory;
import fr.an.dynadapter.alt.ItfId;

public class TstTomcatHttpPingAdapter implements ITstHttpPingSupport {

    protected Resource tomcatResource;

    public TstTomcatHttpPingAdapter(Resource tomcatResource) {
        this.tomcatResource = tomcatResource;
    }

    @Override
    public void ping() {
        if (tomcatResource == null || !tomcatResource.getType().getName().equals("tomcat")) {
            throw new IllegalStateException();
        }
        // mock .. do nothing!
    }
    
    public static class Factory implements IAdapterAlternativeFactory {
        public static final Factory INSTANCE = new Factory();
        
        @Override
        public Object getAdapter(Object adaptableObject, ItfId<?> interfaceId) {
            Resource resource = (Resource) adaptableObject;
            if (resource == null || !resource.getType().getName().equals("tomcat")) {
                throw new IllegalStateException();
            }
            return new TstTomcatHttpPingAdapter(resource);
        }

        @Override
        public ItfId<?>[] getInterfaceIds() {
            return new ItfId[] { ItfId.of(ITstHttpPingSupport.class) };
        }

        @Override
        public String getAlternativeName() {
            return "";
        }
        
    }
}
