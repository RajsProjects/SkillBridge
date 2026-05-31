package com.skillbridge.backend.job;

import com.skillbridge.backend.common.TestDataFactory;
import com.skillbridge.backend.common.enums.UserRole;
import com.skillbridge.backend.common.exception.UnauthorizedException;
import com.skillbridge.backend.job.dto.JobRequest;
import com.skillbridge.backend.job.dto.JobResponse;
import com.skillbridge.backend.user.User;
import com.skillbridge.backend.user.UserRepository;
import com.skillbridge.backend.user.client.ClientProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock JobRepository jobRepository;
    @Mock UserRepository userRepository;
    @Mock ClientProfileRepository clientProfileRepository;

    @InjectMocks JobService jobService;

    @Test
    void post_job_success() {
        User client = TestDataFactory.buildUser(UserRole.CLIENT);
        Job job     = TestDataFactory.buildJob(client);

        JobRequest req = new JobRequest();
        req.setTitle("Build Android App");
        req.setDescription("Need a student to build a basic Android app");
        req.setBudget(new BigDecimal("5000.00"));
        req.setIsRemote(true);

        when(userRepository.findById(any())).thenReturn(Optional.of(client));
        when(jobRepository.save(any())).thenReturn(job);
        when(clientProfileRepository.findByUserId(any())).thenReturn(Optional.empty());

        JobResponse response = jobService.post(client.getId(), req);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Build Android App");
    }

    @Test
    void post_job_throws_when_not_client() {
        User student = TestDataFactory.buildUser(UserRole.STUDENT);

        JobRequest req = new JobRequest();
        req.setTitle("Test");
        req.setDescription("Test description");
        req.setBudget(new BigDecimal("1000"));
        req.setIsRemote(true);

        when(userRepository.findById(any())).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> jobService.post(student.getId(), req))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Only clients can post jobs");
    }
}