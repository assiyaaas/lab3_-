package com.example.bankcase.config;

import com.example.bankcase.domain.CaseStopRule;
import com.example.bankcase.domain.Rule;
import com.example.bankcase.domain.RuleChain;
import com.example.bankcase.domain.TransitionRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RuleConfig {

    @Bean
    public Rule caseRules() {
        return new RuleChain(
                List.of(
                        new TransitionRule(),
                        new CaseStopRule()
                )
        );
    }
}