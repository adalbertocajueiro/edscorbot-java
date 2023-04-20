package es.us.edscorbot.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorDTO {
    private ApplicationError error;
    private String message;
    private String detailedMessage;
}
