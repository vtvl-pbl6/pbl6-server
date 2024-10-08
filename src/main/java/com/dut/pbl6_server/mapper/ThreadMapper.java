package com.dut.pbl6_server.mapper;

import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.config.SpringMapStructConfig;
import com.dut.pbl6_server.dto.respone.AccountResponse;
import com.dut.pbl6_server.dto.respone.ThreadResponse;
import com.dut.pbl6_server.entity.Thread;
import com.dut.pbl6_server.entity.*;
import com.dut.pbl6_server.entity.enums.ThreadStatus;
import com.dut.pbl6_server.entity.json.HosResult;
import org.hibernate.LazyInitializationException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(config = SpringMapStructConfig.class)
public interface ThreadMapper {
    String TO_RESPONSE_NAMED = "thread_to_response";
    ThreadMapper INSTANCE = Mappers.getMapper(ThreadMapper.class);

    @Named(TO_RESPONSE_NAMED)
    @Mapping(source = "author", target = "author", qualifiedByName = "getAuthor")
    @Mapping(source = "parentThread", target = "parentThread", qualifiedByName = "getParentThread")
    @Mapping(source = "thread", target = "content", qualifiedByName = "getContent")
    @Mapping(source = "thread", target = "files", qualifiedByName = "getFiles")
    @Mapping(source = "sharers", target = "sharers", qualifiedByName = "getSharers")
    @Mapping(source = "comments", target = "comments", qualifiedByName = "getComments")
    ThreadResponse toResponse(Thread thread);

    @Named("getAuthor")
    default AccountResponse getAuthor(Account author) {
        try {
            return AccountMapper.INSTANCE.toResponse(author);
        } catch (LazyInitializationException e) {
            return null;
        }
    }

    @Named("getParentThread")
    default ThreadResponse getParentThread(Thread parentThread) {
        try {
            return toResponse(parentThread);
        } catch (LazyInitializationException e) {
            return null;
        }
    }

    @Named("getContent")
    default String getContent(Thread thread) {
        String content = thread.getContent();
        List<HosResult> hosResults = thread.getHosResult();
        if (CommonUtils.String.isEmptyOrNull(content)) return null;
        if (CommonUtils.List.isEmptyOrNull(hosResults)) return content;
        // Replace all hosResult to content
        StringBuilder builder = new StringBuilder();
        int start = 0;
        int end = 0;
        for (int i = 0; i < hosResults.size(); i++) {
            var result = hosResults.get(i);
            end = result.getStart();

            builder.append(content, start, end);
            builder.append("*".repeat(result.getEnd() - result.getStart() + 1));

            start = result.getEnd() + 1;
            if (i == hosResults.size() - 1 && start < content.length()) {
                builder.append(content, start, content.length());
            }
        }
        return builder.isEmpty() ? content : builder.toString();
    }

    @Named("getFiles")
    default List<File> getFiles(Thread thread) {
        try {
            if (CommonUtils.List.isEmptyOrNull(thread.getFiles())) return null;
            return thread.getFiles().stream().map(ThreadFile::getFile).toList();
        } catch (LazyInitializationException e) {
            return null;
        }
    }

    @Named("getComments")
    default List<ThreadResponse> getComments(List<Thread> threadFiles) {
        try {
            if (CommonUtils.List.isEmptyOrNull(threadFiles)) return null;
            return threadFiles.stream().map(this::toResponse).toList();
        } catch (LazyInitializationException e) {
            return null;
        }
    }

    @Named("getSharers")
    default List<AccountResponse> getSharers(List<ThreadSharer> threadSharers) {
        try {
            if (CommonUtils.List.isEmptyOrNull(threadSharers)) return null;
            return threadSharers.stream().map(e -> AccountMapper.INSTANCE.toResponse(e.getUser())).toList();
        } catch (LazyInitializationException e) {
            return null;
        }
    }
}