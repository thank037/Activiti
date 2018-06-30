package ${package};

import com.google.common.collect.Maps;
import org.activiti.engine.*;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class DemoMain {

    private static final Logger logger = LoggerFactory.getLogger(DemoMain.class);

    /**
     * 1. 创建流程引擎
     * 2. 部署流程定义文件
     * 3. 启动运行流程
     * 4. 处理流程任务
     */
    public static void main(String[] args) throws ParseException {

        logger.info("--------开始---------");

        // 1. 创建流程引擎
        ProcessEngine processEngine = getProcessEngine();

        // 2. 部署流程定义文件
        ProcessDefinition processDefinition = getProcessDefinition(processEngine);

        // 3. 启动运行流程
        ProcessInstance processInstance = startProcess(processEngine, processDefinition);

        // 4. 处理流程任务
        processTask(processEngine, processInstance);


        logger.info("--------结束---------");
    }

    private static void processTask(ProcessEngine processEngine, ProcessInstance processInstance) throws ParseException {

        Scanner scanner = new Scanner(System.in);
        while (null!=processInstance && !processInstance.isEnded()) {
            TaskService taskService = processEngine.getTaskService();
            List<Task> taskList = taskService.createTaskQuery().list();
            logger.info("待处理任务数量: {}", taskList.size());

            for (Task task : taskList) {
                logger.info("待处理任务: {}", task.getName());
                Map<String, Object> params = getParams(scanner, processEngine, task);
                taskService.complete(task.getId(), params);
                processInstance = processEngine.getRuntimeService().createProcessInstanceQuery()
                        .processInstanceId(processInstance.getId()).singleResult();
            }
        }
    }

    private static  Map<String, Object> getParams(Scanner scanner, ProcessEngine processEngine, Task task) throws ParseException {
        FormService formService = processEngine.getFormService();
        TaskFormData taskFormData = formService.getTaskFormData(task.getId());
        List<FormProperty> formProperties = taskFormData.getFormProperties();
        Map<String, Object> params = Maps.newHashMap();
        for (FormProperty property : formProperties) {
            String input = null;
            if (StringFormType.class.isInstance(property.getType())) {
                logger.info("请输入{}", property.getName());
                input = scanner.nextLine();
                params.put(property.getId(), input);
            } else if (DateFormType.class.isInstance(property.getType())) {
                logger.info("请输入{}, 格式[yyyy-MM-dd]", property.getName());
                input = scanner.nextLine();
                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(input);
                params.put(property.getId(), date);
            } else {
                logger.info("参数类型不支持: {}", property.getType());
            }
        }

        return params;
    }

    private static ProcessInstance startProcess(ProcessEngine processEngine, ProcessDefinition processDefinition) {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
        logger.info("启动流程: {}", processInstance.getProcessDefinitionKey());
        return processInstance;
    }

    private static ProcessDefinition getProcessDefinition(ProcessEngine processEngine) {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        deploymentBuilder.addClasspathResource("second_approve.bpmn20.xml");
        Deployment deployment = deploymentBuilder.deploy();
        String deploymentId = deployment.getId();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).singleResult();
        logger.info("流程定义对象: 文件:{}, 流程ID:{}", processDefinition.getName(), processDefinition.getId());
        return processDefinition;
    }

    private static ProcessEngine getProcessEngine() {
        ProcessEngineConfiguration cfg = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
        ProcessEngine processEngine = cfg.buildProcessEngine();
        logger.info("流程引擎名称: {}, 版本:{}", processEngine.getName(), ProcessEngine.VERSION);
        return processEngine;
    }
}
