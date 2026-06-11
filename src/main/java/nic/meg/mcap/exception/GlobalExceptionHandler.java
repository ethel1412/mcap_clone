package nic.meg.mcap.exception;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String TRACKING_ID = "trackingId";

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Map<String, Object> body(String message, List<Map<String, String>> errors, HttpServletRequest req) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("message", message);
        if (errors != null && !errors.isEmpty())
            b.put("errors", errors);
        Object tid = req.getAttribute(TRACKING_ID);
        if (tid instanceof String s && !s.isBlank())
            b.put("trackingId", s);
        return b;
    }

    /**
     * Returns a Thymeleaf ModelAndView for browser requests, or a JSON
     * ResponseEntity for API/AJAX requests, based on the Accept header.
     */
    private Object resolve(HttpServletRequest req, HttpStatus status, String message,
                           List<Map<String, String>> errors) {
        String accept = Optional.ofNullable(req.getHeader("Accept")).orElse("");
        if (accept.contains(MediaType.TEXT_HTML_VALUE)) {
            String viewName = "error/" + status.value();
            ModelAndView mav = new ModelAndView(viewName);
            mav.addObject("status", status.value());
            mav.addObject("message", message);
            return mav;
        }
        return ResponseEntity.status(status).body(body(message, errors, req));
    }

    // ── Handlers ──

    // Custom base exception → use its HttpStatus and message
    @ExceptionHandler(PracticeCustomBaseException.class)
    public Object handleCustom(HttpServletRequest req, PracticeCustomBaseException ex) {
        return resolve(req, ex.getStatus(), ex.getMessage(), null);
    }

    // No route matched (404)
    @ExceptionHandler(NoHandlerFoundException.class)
    public Object handleNotFound(HttpServletRequest req, NoHandlerFoundException ex) {
        return resolve(req, HttpStatus.NOT_FOUND, "The requested page could not be found.", null);
    }

    // @RequestBody validation failures
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValid(HttpServletRequest req, MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(), "message",
                        Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid value")))
                .collect(Collectors.toList());
        String first = errors.isEmpty() ? "Validation failed." : errors.get(0).get("message");
        return resolve(req, HttpStatus.BAD_REQUEST, first, errors);
    }

    // Path/query/form param validation (@Validated on method params)
    @ExceptionHandler(ConstraintViolationException.class)
    public Object handleConstraintViolation(HttpServletRequest req, ConstraintViolationException ex) {
        List<Map<String, String>> errors = ex.getConstraintViolations().stream()
                .map(cv -> Map.of("path", String.valueOf(cv.getPropertyPath()),
                        "message", Optional.ofNullable(cv.getMessage()).orElse("Invalid value")))
                .collect(Collectors.toList());
        String first = errors.isEmpty() ? "Validation failed." : errors.get(0).get("message");
        return resolve(req, HttpStatus.BAD_REQUEST, first, errors);
    }

    // Binding errors for form-data / query strings to objects
    @ExceptionHandler(BindException.class)
    public Object handleBindException(HttpServletRequest req, BindException ex) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(), "message",
                        Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid value")))
                .collect(Collectors.toList());
        String first = errors.isEmpty() ? "Invalid request." : errors.get(0).get("message");
        return resolve(req, HttpStatus.BAD_REQUEST, first, errors);
    }

    // Missing required query param
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object handleMissingParam(HttpServletRequest req, MissingServletRequestParameterException ex) {
        return resolve(req, HttpStatus.BAD_REQUEST, "Missing parameter: " + ex.getParameterName(), null);
    }

    // Malformed JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Object handleNotReadable(HttpServletRequest req, HttpMessageNotReadableException ex) {
        return resolve(req, HttpStatus.BAD_REQUEST, "Malformed request body.", null);
    }

    // File upload too large
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object handleMaxSizeException(HttpServletRequest req, MaxUploadSizeExceededException exc) {
        return resolve(req, HttpStatus.PAYLOAD_TOO_LARGE,
                "File is too large! Maximum allowed size is 2MB.", null);
    }

    @ExceptionHandler(InvalidIdentifierException.class)
    public Object handleInvalidIdentifier(HttpServletRequest req, InvalidIdentifierException ex) {
        return resolve(req, HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler({ MethodArgumentTypeMismatchException.class, TypeMismatchException.class,
            IllegalArgumentException.class, NullPointerException.class })
    public Object handleTypeMismatch(HttpServletRequest req, Exception ex) {
        return resolve(req, HttpStatus.BAD_REQUEST, "Invalid parameter value.", null);
    }

    // Fallback – return generic 500; details are logged server-side only
    @ExceptionHandler(Exception.class)
    public Object handleGeneral(HttpServletRequest req, Exception ex) {
        return resolve(req, HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.", null);
    }
}
