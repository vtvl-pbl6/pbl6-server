package com.dut.pbl6_server.repository.fetch_data.impl;

import com.dut.pbl6_server.entity.Thread;
import com.dut.pbl6_server.repository.fetch_data.ThreadsFetchRepository;
import com.dut.pbl6_server.repository.fetch_data.base.FetchBaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ThreadsFetchRepositoryImpl implements ThreadsFetchRepository {
    private final FetchBaseRepository<Thread> fetchBaseRepository;
    
    @Override
    public FetchBaseRepository<Thread> getRepository() {
        return fetchBaseRepository;
    }
}
