package org.com.batchservice.writter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.batchservice.dto.response.AccountDto;
import org.com.batchservice.service.EmailService;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActiveUserWriterCustom implements ItemWriter<AccountDto> {
    private final EmailService emailService;
    @Override
    public void write(Chunk<? extends AccountDto> chunk) throws Exception {
        List<String> accountEmails= chunk.getItems().stream().map(AccountDto::getEmail).toList();
        String[] emails = accountEmails.toArray(new String[0]);
        String subject="Thông báo tài khoản đã đăng kí thành công";
        String htmlContent= "<h1>Vui lòng truy cập url này để đăng nhập: http://dtuforyou/login</h1>";
        emailService.sendHtmlEmail(emails,subject,htmlContent);
    }
}
