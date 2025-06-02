package com.example.plugin.report.example;  
  
import io.onedev.server.web.page.project.builds.detail.report.BuildReportPage;  
import org.apache.wicket.Component;  
import org.apache.wicket.markup.html.basic.Label;  
import org.apache.wicket.markup.html.list.ListItem;  
import org.apache.wicket.markup.html.list.ListView;  
import org.apache.wicket.markup.html.panel.Fragment;  
import org.apache.wicket.model.IModel;  
import org.apache.wicket.model.LoadableDetachableModel;  
import org.apache.wicket.request.mapper.parameter.PageParameters;  
import java.util.List;
  
public class ExampleReportPage extends BuildReportPage {  
  
    private final IModel<ExampleReport> reportModel = new LoadableDetachableModel<ExampleReport>() {  
        @Override  
        protected ExampleReport load() {  
            return null;
            //return OneDev.getInstance(BuildManager.class).getReportData(getBuild(), getReportName(), ExampleReport.class);  
        }  
    };  
      
    public ExampleReportPage(PageParameters params) {  
        super(params);  
    }  
      
    @Override  
    protected void onInitialize() {  
        super.onInitialize();  
          
        if (getReport() != null) {  
            Fragment fragment = new Fragment("report", "validFrag", this);  
            fragment.add(new ListView<ExampleReport.ExampleItem>("items", 
                    new LoadableDetachableModel<List<ExampleReport.ExampleItem>>() {
                        @Override
                        protected List<ExampleReport.ExampleItem> load() {
                            return getReport().getItems();
                        }
                    }) {
                @Override
                protected void populateItem(ListItem<ExampleReport.ExampleItem> item) {
                    ExampleReport.ExampleItem exampleItem = item.getModelObject();
                    item.add(new Label("name", exampleItem.getName()));
                    item.add(new Label("value", exampleItem.getValue()));
                }
            });
              
            add(fragment);  
        } else {  
            add(new Fragment("report", "invalidFrag", this));  
        }  
    }  
      
    private ExampleReport getReport() {  
        return reportModel.getObject();  
    }  
      
    @Override  
    public String getReportName() {  
        return "example";  
    }  
      
    @Override  
    protected Component newProjectTitle(String componentId) {  
        return new Label(componentId, "Example Report");  
    }  
}