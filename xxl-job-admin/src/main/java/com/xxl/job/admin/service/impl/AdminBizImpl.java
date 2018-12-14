package com.xxl.job.admin.service.impl;

import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobLog;
import com.xxl.job.admin.core.thread.JobTriggerPoolHelper;
import com.xxl.job.admin.core.trigger.TriggerTypeEnum;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.dao.XxlJobLogDao;
import com.xxl.job.admin.dao.XxlJobRegistryDao;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.HandleCallbackParam;
import com.xxl.job.core.biz.model.HandlerRegistryParam;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xuxueli 2017-07-27 21:54:20
 */
@Service
public class AdminBizImpl implements AdminBiz {
    private static Logger logger = LoggerFactory.getLogger(AdminBizImpl.class);

    @Resource
    public XxlJobLogDao xxlJobLogDao;
    @Resource
    private XxlJobInfoDao xxlJobInfoDao;
    @Resource
    private XxlJobRegistryDao xxlJobRegistryDao;
    @Resource
    private XxlJobGroupDao xxlJobGroupDao;


    @Override
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList) {
        for (HandleCallbackParam handleCallbackParam: callbackParamList) {
            ReturnT<String> callbackResult = callback(handleCallbackParam);
            logger.info(">>>>>>>>> JobApiController.callback {}, handleCallbackParam={}, callbackResult={}",
                    (callbackResult.getCode()==IJobHandler.SUCCESS.getCode()?"success":"fail"), handleCallbackParam, callbackResult);
        }

        return ReturnT.SUCCESS;
    }

    private ReturnT<String> callback(HandleCallbackParam handleCallbackParam) {
        // valid log item
        XxlJobLog log = xxlJobLogDao.load(handleCallbackParam.getLogId());
        if (log == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "log item not found.");
        }
        if (log.getHandleCode() > 0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "log repeate callback.");     // avoid repeat callback, trigger child job etc
        }

        // trigger success, to trigger child job
        String callbackMsg = null;
        if (IJobHandler.SUCCESS.getCode() == handleCallbackParam.getExecuteResult().getCode()) {
            XxlJobInfo xxlJobInfo = xxlJobInfoDao.loadById(log.getJobId());
            if (xxlJobInfo!=null && StringUtils.isNotBlank(xxlJobInfo.getChildJobId())) {
                callbackMsg = "<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>"+ I18nUtil.getString("jobconf_trigger_child_run") +"<<<<<<<<<<< </span><br>";

                String[] childJobIds = xxlJobInfo.getChildJobId().split(",");
                for (int i = 0; i < childJobIds.length; i++) {
                    int childJobId = (StringUtils.isNotBlank(childJobIds[i]) && StringUtils.isNumeric(childJobIds[i]))?Integer.valueOf(childJobIds[i]):-1;
                    if (childJobId > 0) {

                        JobTriggerPoolHelper.trigger(childJobId, TriggerTypeEnum.PARENT, -1, null, null);
                        ReturnT<String> triggerChildResult = ReturnT.SUCCESS;

                        // add msg
                        callbackMsg += MessageFormat.format(I18nUtil.getString("jobconf_callback_child_msg1"),
                                (i+1),
                                childJobIds.length,
                                childJobIds[i],
                                (triggerChildResult.getCode()==ReturnT.SUCCESS_CODE?I18nUtil.getString("system_success"):I18nUtil.getString("system_fail")),
                                triggerChildResult.getMsg());
                    } else {
                        callbackMsg += MessageFormat.format(I18nUtil.getString("jobconf_callback_child_msg2"),
                                (i+1),
                                childJobIds.length,
                                childJobIds[i]);
                    }
                }

            }
        }

        // handle msg
        StringBuffer handleMsg = new StringBuffer();
        if (log.getHandleMsg()!=null) {
            handleMsg.append(log.getHandleMsg()).append("<br>");
        }
        if (handleCallbackParam.getExecuteResult().getMsg() != null) {
            handleMsg.append(handleCallbackParam.getExecuteResult().getMsg());
        }
        if (callbackMsg != null) {
            handleMsg.append(callbackMsg);
        }

        // success, save log
        log.setHandleTime(new Date());
        log.setHandleCode(handleCallbackParam.getExecuteResult().getCode());
        log.setHandleMsg(handleMsg.toString());
        xxlJobLogDao.updateHandleInfo(log);

        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> registry(RegistryParam registryParam) {
        int ret = xxlJobRegistryDao.registryUpdate(registryParam.getRegistGroup(), registryParam.getRegistryKey(), registryParam.getRegistryValue());
        if (ret < 1) {
            xxlJobRegistryDao.registrySave(registryParam.getRegistGroup(), registryParam.getRegistryKey(), registryParam.getRegistryValue());
        }
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> registryRemove(RegistryParam registryParam) {
        xxlJobRegistryDao.registryDelete(registryParam.getRegistGroup(), registryParam.getRegistryKey(), registryParam.getRegistryValue());
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> registryHandler(HandlerRegistryParam handlerRegistryParam) {
        String jobGroupName = handlerRegistryParam.getExecutor();
        List<XxlJobGroup> xxlJobGroups = xxlJobGroupDao.findAll();
        Map<String, XxlJobGroup> xxlJobGroupMap = xxlJobGroups.stream().collect(Collectors.toMap(XxlJobGroup::getAppName, xxlJobGroup -> xxlJobGroup));
        XxlJobGroup jobGroup = xxlJobGroupMap.get(jobGroupName);
        if (jobGroup == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, String.format(">>>>>>>>> registryHandler fail. %s not exists.", jobGroupName));
        }
        XxlJobInfo xxlJobInfo = new XxlJobInfo();
        xxlJobInfo.setJobGroup(jobGroup.getId());

        xxlJobInfo.setJobCron(handlerRegistryParam.getJobCron());
        xxlJobInfo.setJobDesc(handlerRegistryParam.getJobDesc());
        xxlJobInfo.setAuthor(handlerRegistryParam.getAuthor());
        xxlJobInfo.setAlarmEmail(handlerRegistryParam.getAlarmEmail());
        xxlJobInfo.setExecutorRouteStrategy(handlerRegistryParam.getExecutorRouteStrategy());
        xxlJobInfo.setExecutorHandler(handlerRegistryParam.getExecutorHandler());
        xxlJobInfo.setExecutorBlockStrategy(handlerRegistryParam.getExecutorBlockStrategy());
        xxlJobInfo.setExecutorTimeout(handlerRegistryParam.getExecutorTimeout());
        xxlJobInfo.setExecutorFailRetryCount(handlerRegistryParam.getExecutorFailRetryCount());
        xxlJobInfo.setGlueType(handlerRegistryParam.getGlueType());
        xxlJobInfo.setGlueUpdatetime(new Date());
        Map<String, XxlJobInfo> xxlJobsInGroup = xxlJobInfoDao.getJobsByGroup(jobGroup.getId())
                .stream().collect(Collectors.toMap(XxlJobInfo::getExecutorHandler, temp -> temp));

        XxlJobInfo origin = xxlJobsInGroup.get(xxlJobInfo.getExecutorHandler());
        if (origin != null && StringUtils.isBlank(origin.getExecutorParam())) {
            xxlJobInfo.setId(xxlJobsInGroup.get(xxlJobInfo.getExecutorHandler()).getId());
            xxlJobInfoDao.update(xxlJobInfo);
        } else {
            xxlJobInfoDao.save(xxlJobInfo);
        }

        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> registryHandler(List<HandlerRegistryParam> handlerRegistryParams) {
        handlerRegistryParams.forEach(this::registryHandler);
        return ReturnT.SUCCESS;
    }

}
