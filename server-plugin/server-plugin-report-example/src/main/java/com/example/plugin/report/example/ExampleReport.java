package com.example.plugin.report.example;  
  
import java.io.Serializable;  
import java.util.List;  
  
public class ExampleReport implements Serializable {  
      
    private static final long serialVersionUID = 1L;  
      
    private final List<ExampleItem> items;  
      
    public ExampleReport(List<ExampleItem> items) {  
        this.items = items;  
    }  
      
    public List<ExampleItem> getItems() {  
        return items;  
    }  
      
    public static class ExampleItem implements Serializable {  
          
        private static final long serialVersionUID = 1L;  
          
        private final String name;  
        private final String value;  
          
        public ExampleItem(String name, String value) {  
            this.name = name;  
            this.value = value;  
        }  
          
        public String getName() {  
            return name;  
        }  
          
        public String getValue() {  
            return value;  
        }  
    }  
}