package com.example.plugin.report.example;  
  
import io.onedev.commons.codeassist.InputSuggestion;  
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.ServerStepResult;
import io.onedev.server.annotation.Editable;  
import io.onedev.server.annotation.Interpolative;  
import io.onedev.server.annotation.Patterns;  
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.step.PublishReportStep;
import io.onedev.server.buildspec.step.StepGroup;  
import io.onedev.server.model.Build;
import javax.validation.constraints.NotEmpty;  
import java.io.File;  
import java.util.ArrayList;  
import java.util.List;  
  
@Editable(order=10000, group=StepGroup.PUBLISH, name="Example Report")  
public class PublishExampleReportStep extends PublishReportStep {  
  
    private static final long serialVersionUID = 1L;  
      
    @Editable(order=100, description="Specify example report file relative to job workspace, "  
            + "for instance, <tt>target/example-report.json</tt>. Use * or ? for pattern match")  
    @Interpolative(variableSuggester="suggestVariables")  
    @Patterns(path=true)  
    @NotEmpty  
    @Override  
    public String getFilePatterns() {  
        return super.getFilePatterns();  
    }  
  
    @Override  
    public void setFilePatterns(String filePatterns) {  
        super.setFilePatterns(filePatterns);  
    }  
      
    @SuppressWarnings("unused")  
    private static List<InputSuggestion> suggestVariables(String matchWith) {  
        return BuildSpec.suggestVariables(matchWith, true, true, false);  
    }  
protected ExampleReport process(Build build, File inputDir, TaskLogger logger) {  
    // 实现处理报告文件的逻辑  
    // 这里只是一个简单的示例  
    List<ExampleReport.ExampleItem> items = new ArrayList<>();  
      
    for (File file: getPatternSet().listFiles(inputDir)) {  
        // 解析文件内容并创建报告项  
        items.add(new ExampleReport.ExampleItem("File", file.getName()));  
    }  
      
    return new ExampleReport(items);  
}

   @Override
   public ServerStepResult run(Long buildId, File inputDir, TaskLogger logger) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'run'");
   }  
}