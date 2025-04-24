package com.example.wx.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author wangxiang
 * @description
 * @create 2025/4/23 16:37
 */
public class OrchestratorWorkers {

    private final ChatClient chatClient;
    private final String orchestratorPrompt;
    private final String workerPrompt;


    public static final String DEFAULT_ORCHESTRATOR_PROMPT = """
    分析这个任务，并将其拆解为 2-3 种不同的处理方式：

    任务: {task}

    请按以下 JSON 格式返回你的回答：
    \\{
        "analysis": "解释你对该任务的理解，以及为什么这些变体是有价值的。
                     重点说明每种方法如何从不同角度处理任务。",
        "tasks": [
            \\{
                "type": "formal",
                "description": "撰写一个精确、技术性的版本，强调具体规范"
                \\},
                \\{
                "type": "conversational",
                "description": "撰写一个有吸引力、友好的版本，以便与读者建立联系"
            \\}
        ]
    \\}
    """;


    public static final String DEFAULT_WORKER_PROMPT = """
			根据以下信息生成内容:
			任务: {original_task}
			风格: {task_type}
			指导说明: {task_description}
			""";

    public OrchestratorWorkers(ChatClient chatClient) {
        this(chatClient, DEFAULT_ORCHESTRATOR_PROMPT, DEFAULT_WORKER_PROMPT);
    }

    public OrchestratorWorkers(ChatClient chatClient, String orchestratorPrompt, String workerPrompt) {
        Assert.notNull(chatClient, "ChatClient must not be null");
        Assert.hasText(orchestratorPrompt, "Orchestrator prompt must not be empty");
        Assert.hasText(workerPrompt, "Worker prompt must not be empty");

        this.chatClient = chatClient;
        this.orchestratorPrompt = orchestratorPrompt;
        this.workerPrompt = workerPrompt;
    }

    public static record Task(String type, String description) {
    }

    public static record OrchestratorResponse(String analysis, List<Task> tasks) {
    }

    public static record FinalResponse(String analysis, List<String> workerResponses) {
    }

    public FinalResponse process(String taskDescription) {
        Assert.hasText(taskDescription, "任务描述不能为空");

        OrchestratorResponse orchestratorResponse = this.chatClient.prompt()
                .user(u -> u.text(this.orchestratorPrompt)
                        .param("task", taskDescription))
                .call()
                .entity(OrchestratorResponse.class);

        System.out.printf("\n=== 协调器 输出 ===\nANALYSIS: %s\n\n任务列表: %s\n",
                orchestratorResponse.analysis(), orchestratorResponse.tasks());

        List<String> workerResponses = orchestratorResponse.tasks().stream().map(task -> this.chatClient.prompt()
                .user(u -> u.text(this.workerPrompt)
                        .param("original_task", taskDescription)
                        .param("task_type", task.type())
                        .param("task_description", task.description()))
                .call()
                .content()).toList();

        System.out.println("\n=== 执行器输出 ===\n" + workerResponses);

        return new FinalResponse(orchestratorResponse.analysis(), workerResponses);
    }
}


