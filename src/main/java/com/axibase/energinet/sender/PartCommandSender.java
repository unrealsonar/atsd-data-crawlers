package com.axibase.energinet.sender;

import com.axibase.tsd.client.DataService;
import com.axibase.tsd.model.data.command.SendCommandResult;
import com.axibase.tsd.network.PlainCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PartCommandSender {
    private static final Logger log = LoggerFactory.getLogger(PartCommandSender.class);
    private static final Integer DEFAULT_PART_SIZE = 50;
    private Integer partSize = DEFAULT_PART_SIZE;
    private DataService dataService;

    public PartCommandSender(DataService dataService) {
        this.dataService = dataService;
    }

    public PartCommandSender(DataService dataService, Integer partSize) {
        this(dataService);
    }

    public Integer getPartSize() {
        return this.partSize;
    }

    public void setPartSize(Integer partSize) {
        this.partSize = partSize;
    }

    public void send(Collection<PlainCommand> commands) {
        Integer failCount = 0;
        List<PlainCommand> batch = new ArrayList<>();
        for (PlainCommand command : commands) {
            if (batch.size() < this.partSize) {
                batch.add(command);
            } else {
                SendCommandResult result = this.dataService.sendBatch(batch).getResult();
                failCount = failCount + result.getFail();
                batch.clear();
                failCount = failCount + result.getFail();
            }
        }
        SendCommandResult result = this.dataService.sendBatch(batch).getResult();
        failCount = failCount + result.getFail();
        batch.clear();
        log.info("Command send result: total: {}, fail: {}", commands.size(), failCount);
    }
}