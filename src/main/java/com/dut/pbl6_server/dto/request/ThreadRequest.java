package com.dut.pbl6_server.dto.request;

import com.dut.pbl6_server.annotation.json.JsonSnakeCaseNaming;
import com.dut.pbl6_server.entity.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonSnakeCaseNaming
public class ThreadRequest {
    private String content;
    private Long parentId;
    private List<MultipartFile> files;
    private Visibility visibility;
}
