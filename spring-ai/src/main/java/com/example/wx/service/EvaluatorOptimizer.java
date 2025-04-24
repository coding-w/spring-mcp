package com.example.wx.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangxiang
 * @description
 * @create 2025/4/23 17:13
 */
public class EvaluatorOptimizer {
    public static final String DEFAULT_GENERATOR_PROMPT = """
			你的目标是根据输入完成任务。如果之前的生成结果中有反馈信息，你应当根据这些反馈优化你的解决方案。
		
			重要提示：你的回复必须是**一行**有效的 JSON 格式，**不得包含换行符**，除非你显式使用 \\n 转义。
		
			以下是你必须遵循的准确格式，包括所有引号和大括号：
		
			{"thoughts":"简要说明","response":"public class Example {\\n    // 代码写在这里\\n}"}
		
			response 字段的规则：
			1. 所有换行必须使用 \\n
			2. 所有引号必须使用 \\"
			3. 所有反斜杠必须双写：\\
			4. 不允许实际换行或格式化——所有内容必须在一行内
			5. 不允许制表符或特殊字符
			6. Java 代码必须完整且正确转义
		
			正确格式的示例：
			{"thoughts":"实现计数器","response":"public class Counter {\\n    private int count;\\n    public Counter() {\\n        count = 0;\\n    }\\n    public void increment() {\\n        count++;\\n    }\\n}"}
		
			**必须严格遵循此格式** —— 回复必须是一行合法 JSON。
			""";


    public static final String DEFAULT_EVALUATOR_PROMPT = """
			请评估此代码实现的正确性、时间复杂度以及是否符合最佳实践。
			确保代码包含规范的 Javadoc 注释。
			回复必须是单行 JSON 格式，格式如下：
			
			{"evaluation":"PASS, NEEDS_IMPROVEMENT, or FAIL", "feedback":"你的反馈内容"}
			
			evaluation 字段必须是以下之一："PASS"、"NEEDS_IMPROVEMENT"、"FAIL"
			只有在所有标准都完全达成且无任何改进建议时，才使用 "PASS"。
			""";

    private final ChatClient chatClient;

    private final String generatorPrompt;

    private final String evaluatorPrompt;

    public static record Generation(String thoughts, String response) {
    }

    public static record EvaluationResponse(Evaluation evaluation, String feedback) {

        public enum Evaluation {
            PASS, NEEDS_IMPROVEMENT, FAIL
        }
    }

    public static record RefinedResponse(String solution, List<Generation> chainOfThought) {
    }

    public EvaluatorOptimizer(ChatClient chatClient) {
        this(chatClient, DEFAULT_GENERATOR_PROMPT, DEFAULT_EVALUATOR_PROMPT);
    }

    public EvaluatorOptimizer(ChatClient chatClient, String generatorPrompt, String evaluatorPrompt) {
        Assert.notNull(chatClient, "ChatClient must not be null");
        Assert.hasText(generatorPrompt, "Generator prompt must not be empty");
        Assert.hasText(evaluatorPrompt, "Evaluator prompt must not be empty");

        this.chatClient = chatClient;
        this.generatorPrompt = generatorPrompt;
        this.evaluatorPrompt = evaluatorPrompt;
    }

    public RefinedResponse loop(String task) {
        List<String> memory = new ArrayList<>();
        List<Generation> chainOfThought = new ArrayList<>();

        return loop(task, "", memory, chainOfThought);
    }

    private RefinedResponse loop(String task, String context, List<String> memory,
                                 List<Generation> chainOfThought) {

        Generation generation = generate(task, context);
        memory.add(generation.response());
        chainOfThought.add(generation);

        EvaluationResponse evaluationResponse = evalute(generation.response(), task);

        if (evaluationResponse.evaluation().equals(EvaluationResponse.Evaluation.PASS)) {
            // 通过
            return new RefinedResponse(generation.response(), chainOfThought);
        }

        // 重新构建提示词
        StringBuilder newContext = new StringBuilder();
        newContext.append("Previous attempts:");
        for (String m : memory) {
            newContext.append("\n- ").append(m);
        }
        newContext.append("\nFeedback: ").append(evaluationResponse.feedback());

        return loop(task, newContext.toString(), memory, chainOfThought);
    }

    private Generation generate(String task, String context) {
        Generation generationResponse = chatClient.prompt()
                .user(u -> u.text("{prompt}\n{context}\nTask: {task}")
                        .param("prompt", this.generatorPrompt)
                        .param("context", context)
                        .param("task", task))
                .call()
                .entity(Generation.class);

        System.out.println(String.format("\n=== GENERATOR OUTPUT ===\nTHOUGHTS: %s\n\nRESPONSE:\n %s\n",
                generationResponse.thoughts(), generationResponse.response()));
        return generationResponse;
    }


    private EvaluationResponse evalute(String content, String task) {

        EvaluationResponse evaluationResponse = chatClient.prompt()
                .user(u -> u.text("{prompt}\nOriginal task: {task}\nContent to evaluate: {content}")
                        .param("prompt", this.evaluatorPrompt)
                        .param("task", task)
                        .param("content", content))
                .call()
                .entity(EvaluationResponse.class);

        System.out.println(String.format("\n=== EVALUATOR OUTPUT ===\nEVALUATION: %s\n\nFEEDBACK: %s\n",
                evaluationResponse.evaluation(), evaluationResponse.feedback()));
        return evaluationResponse;
    }
}
