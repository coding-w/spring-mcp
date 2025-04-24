## Spring + AI 应用文档

本项目基于 Spring 框架，结合 AI 模型的能力，设计了一系列面向不同工作流场景的组件。以下文档按照 `ChainWorkflow` → `ParallelizationWorkflow` → `RoutingWorkflow` → `OrchestratorWorkers` → `EvaluatorOptimizer` 的顺序进行模块分析和应用说明。

---

### 1. ChainWorkflow：链式工作流

**模块简介：**  
ChainWorkflow 实现了多个 AI 处理步骤的顺序执行流程。每个步骤处理并传递结果给下一个步骤，类似管道。

**主要功能：**

- 使用 Spring AI 的 `PromptTemplate` 和 `ChatClient` 实现链式调用
- 每一步可自定义 prompt 模板和响应处理逻辑

**核心实现逻辑：**

```java
PromptTemplate template = new PromptTemplate("You are an assistant. Answer: {input}");
String prompt = template.createMessage(Map.of("input", "What is Spring AI?"));
String response = chatClient.call(prompt);
```

**应用场景：**  
适用于需要串行处理多步逻辑的场景，例如数据清洗 → 分类 → 生成摘要。

---

### 2. ParallelizationWorkflow：并行化工作流

**模块简介：**  
该模块通过并行执行多个 AI 任务，提高响应效率。

**主要功能：**

- 使用 Java 的并发框架（如 CompletableFuture）并行发送多个请求
- 聚合多个任务的返回结果

**核心实现逻辑：**

```java
CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> chatClient.call(prompt1));
CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> chatClient.call(prompt2));
String combined = task1.get() + task2.get();
```

**应用场景：**  
适合多个独立问题需同时回答，如智能客服多个候选答复。

---

### 3. RoutingWorkflow：路由工作流

**模块简介：**  
根据输入内容动态选择不同的 AI 处理策略或模型。

**主要功能：**

- 基于输入特征（如关键词）选择不同 prompt 或模型
- 实现 AI 处理的分流与定制化

**核心实现逻辑：**

```java
if (input.contains("math")) {
    return mathPromptClient.call(input);
} else {
    return generalPromptClient.call(input);
}
```

**应用场景：**  
适用于构建多任务型 AI 应用，如客服系统中根据意图选择应答逻辑。

---

### 4. OrchestratorWorkers：编排器与子任务

**模块简介：**  
通过主任务协调多个子任务，类似调度中心。

**主要功能：**

- 主任务生成子任务，控制执行顺序和依赖
- 子任务间结果可互为输入

**核心实现逻辑：**

```java
String resultA = stepA.execute(input);
String resultB = stepB.execute(resultA);
String finalResult = stepC.execute(resultB);
```

**应用场景：**  
适合需要高度流程控制的场景，如自动文档撰写流程（提纲 → 内容 → 总结）。

---

### 5. EvaluatorOptimizer：评估与优化器

**模块简介：**  
用于对 AI 输出结果进行评估与优化。

**主要功能：**

- 实现输出质量的评价逻辑（如打分、标签）
- 基于评估结果决定是否优化或重试

**核心实现逻辑：**

```java
int score = evaluator.score(response);
if (score < threshold) {
    response = chatClient.retry(prompt);
}
```

**应用场景：**  
适用于对 AI 响应质量要求高的场景，例如教育答题系统或内容生成审核。

---

此项目展示了 Spring 与 AI 的深度结合，支持链式、并行、路由、编排及优化等多种工作流形式，适用于构建各类智能化企业应用。