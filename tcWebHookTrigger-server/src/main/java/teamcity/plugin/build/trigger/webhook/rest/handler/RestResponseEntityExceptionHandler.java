package teamcity.plugin.build.trigger.webhook.rest.handler;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import teamcity.plugin.build.trigger.webhook.exception.BuildTypeNotFoundException;
import teamcity.plugin.build.trigger.webhook.exception.PermissionedDeniedException;
import teamcity.plugin.build.trigger.webhook.exception.UnparsablePayloadException;
import teamcity.plugin.build.trigger.webhook.exception.WebException;
import teamcity.plugin.rest.core.Loggers;
import teamcity.plugin.rest.core.handler.CoreRestResponseEntityExceptionHandler.Error;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	public RestResponseEntityExceptionHandler() {
		Loggers.SERVER.info("RestResponseEntityExceptionHandler :: Starting");
	}

	@ExceptionHandler(value = { BuildTypeNotFoundException.class, PermissionedDeniedException.class, UnparsablePayloadException.class })
	public ResponseEntity<Object> handleServerStop(WebException ex, WebRequest request) {
		return handleExceptionInternal(ex, new Error(ex.getStatusCode(), ex.getMessage()), new HttpHeaders(),
				HttpStatus.valueOf(ex.getStatusCode()), request);
	}

}