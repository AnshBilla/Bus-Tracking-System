package com.smartRahi.SmartRahi.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
// import java.time.LocalTime; // Is import ki yahaan zaroorat nahi hai

@Data // <-- Yeh @Getter, @Setter, @ToString, @EqualsAndHashCode laga dega
@NoArgsConstructor // <-- Khali constructor banayega
@AllArgsConstructor // <-- Saare arguments wala constructor banayega
@Builder
public class JourneyPlanResponse {

    // Is class mein sirf yeh ek field honi chahiye
    private List<JourneyOption> options;

}