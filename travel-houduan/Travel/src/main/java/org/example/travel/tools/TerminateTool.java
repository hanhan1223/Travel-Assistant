package org.example.travel.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 终止工具（作用是让自主规划智能体能够合理地中断）
 */
@Component
public class TerminateTool {

    @Tool(description = """
            当你已经完成用户的请求，或者无法继续处理任务时，调用此工具结束对话。
            在调用此工具前，请确保已经给出了完整的回答。
            """)
    public String doTerminate(
            @ToolParam(description = "给用户的最终回答") String finalAnswer
    ) {
        return finalAnswer != null ? finalAnswer : "任务结束";
    }
}
