package com.exampleodata.demo.controller;

import com.exampleodata.demo.data.StorageForAction;
import com.exampleodata.demo.model.*;
import org.apache.olingo.commons.api.edmx.EdmxReference;
        import org.apache.olingo.server.api.OData;
        import org.apache.olingo.server.api.ODataHttpHandler;
        import org.apache.olingo.server.api.ServiceMetadata;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
        import org.springframework.web.bind.annotation.RestController;

        import javax.servlet.http.HttpServletRequest;
        import javax.servlet.http.HttpServletRequestWrapper;
        import javax.servlet.http.HttpServletResponse;
        import javax.servlet.http.HttpSession;
        import java.util.ArrayList;

@RestController
@RequestMapping("/odataforaction")
public class EDMForAllForActionController {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(EDMForAllForActionController.class);

    public static final String URIForAll = "/odataforaction";



    @GetMapping("/**")
    @ResponseBody
    public void process(HttpServletRequest request, HttpServletResponse response) {
        try{
            HttpSession session = request.getSession(true);
            StorageForAction storage = (StorageForAction) session.getAttribute(StorageForAction.class.getName());
            if (storage == null) {
                storage = new StorageForAction();
                session.setAttribute(StorageForAction.class.getName(), storage);
            }
            OData odata = OData.newInstance();
            ServiceMetadata edm = odata.createServiceMetadata(new DemoEdmProviderForAllForAction(),
                    new ArrayList<EdmxReference>());
            ODataHttpHandler handler = odata.createHandler(edm);
            handler.register(new DemoEntityCollectionProcessorForAction(storage));
            handler.register(new DemoEntityProcessor(storage));
            handler.register(new DemoPrimitiveProcessor(storage));
            handler.register(new DemoActionProcessor(storage));
            handler.process(new HttpServletRequestWrapper(request) {

                @Override
                public String getServletPath() {
                    return EDMForAllForActionController.URIForAll;
                }
            }, response);
        } catch (RuntimeException e) {
            LOG.error("Server Error occurred in DemoServlet", e);
            throw e;
        }
    }
}
