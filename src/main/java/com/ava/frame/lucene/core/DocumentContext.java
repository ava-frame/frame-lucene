package com.ava.frame.lucene.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
@Component
public class DocumentContext {

    private static Log log = LogFactory.getLog(DocumentContext.class);
//    lucene存放地址
    @Value("${lucene.index.path}")
    public String  indexPath;
    @Autowired
    public List<DataRunner> dataRunners;
    private static Map<Class, DocumentDefinition> classDocumentDefinitionMap = new HashMap<Class, DocumentDefinition>();

    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);


    public static DocumentDefinition findDocumentDefinition(Class clazz) {
        DocumentDefinition documentDefinition = classDocumentDefinitionMap.get(clazz);
        if (documentDefinition == null) {
            throw new RuntimeException(clazz.getSimpleName() + " document not found");
        }
        return documentDefinition;
    }

    /**
     * 初始化lucene，注册并开启增量更新，包含写进程，不能同时启动2个或以上，
     *
     * @throws Exception
     */
    public void init() throws Exception {
        if (dataRunners == null || dataRunners.isEmpty()) {
            return;
        }
        register();
        start();
    }

    /**
     * 只允许读，可同时开启多个线程
     *
     * @throws Exception
     */
    public void initReader() throws Exception {
        registerReader();
        startReaderCurrent();
    }

    public void startReaderCurrent() {
        for (final DataRunner dataRunner : dataRunners) {
            try {
                log.info(dataRunner.clazz().getSimpleName() + " current ");
                DocumentDefinition documentDefinition = findDocumentDefinition(dataRunner.clazz());
                documentDefinition.current();
            } catch (Exception ex) {
                log.error(dataRunner.clazz().getSimpleName() + " current ", ex);
            }
        }
    }

    /**
     * 写进程，+搜索，只能允许一个进程存在
     *
     * @throws Exception
     */
    public void initNoRate() throws Exception {
        if (dataRunners == null || dataRunners.isEmpty()) {
            return;
        }
        register();
        startNoRate();
    }

    /**
     * 用于datatool只运行一次。
     */
    public synchronized void startNoRate() {
        for (final DataRunner dataRunner : dataRunners) {
            try {
                log.info(dataRunner.clazz().getSimpleName() + " start ");
                DocumentDefinition documentDefinition = findDocumentDefinition(dataRunner.clazz());
                documentDefinition.index(dataRunner);
            } catch (Exception ex) {
                log.error(dataRunner.clazz().getSimpleName() + " start ", ex);
            }
        }
    }

    private void start() {
        for (final DataRunner dataRunner : dataRunners) {
            scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        log.info(dataRunner.clazz().getSimpleName() + " start ");
                        DocumentDefinition documentDefinition = findDocumentDefinition(dataRunner.clazz());
                        documentDefinition.index(dataRunner);
                    } catch (Exception ex) {
                        log.error(dataRunner.clazz().getSimpleName() + " start ", ex);
                    }
                }
            }, 3, dataRunner.time(), TimeUnit.SECONDS);
        }
    }

    private void registerReader() {
        for (final DataRunner dataRunner : dataRunners) {
            if (classDocumentDefinitionMap.containsKey(dataRunner.clazz())) {
                throw new RuntimeException("double register dataRunner");
            }
            classDocumentDefinitionMap.put(dataRunner.clazz(), new DocumentDefinition(indexPath, dataRunner.clazz(), true));
        }
    }

    public void register() {
        for (final DataRunner dataRunner : dataRunners) {
            if (classDocumentDefinitionMap.containsKey(dataRunner.clazz())) {
//                DocumentDefinition documentDefinition= classDocumentDefinitionMap.get(dataRunner.clazz());
//                documentDefinition.close();
                throw new RuntimeException("double register dataRunner");
            }
            classDocumentDefinitionMap.put(dataRunner.clazz(), new DocumentDefinition(indexPath, dataRunner.clazz(), false));
        }
    }


    public List<DataRunner> getDataRunners() {
        return dataRunners;
    }

    public void setDataRunners(List<DataRunner> dataRunners) {
        this.dataRunners = dataRunners;
    }


    public String getIndexPath() {
        return indexPath;
    }

    public void setIndexPath(String indexPath) {
        this.indexPath = indexPath;
    }


}
