package me.travja.crave.common.filters;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.NoSuchElementException;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

@Component
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse httpResponse) throws IOException {

//        if (httpResponse.getBody() != null) httpResponse.getBody().reset();
        if (httpResponse.getStatusCode() == HttpStatus.UNAUTHORIZED)// && httpResponse.getBody().available() > 0)
            return false;

        return (httpResponse.getStatusCode().series() == CLIENT_ERROR
                || httpResponse.getStatusCode().series() == SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse) throws IOException {

        if (httpResponse.getStatusCode()
                .series() == HttpStatus.Series.SERVER_ERROR) {
            // handle SERVER_ERROR
            throw new HttpClientErrorException(httpResponse.getStatusCode(), httpResponse.getStatusText(),
                    httpResponse.getBody().readAllBytes(), Charset.defaultCharset());
        } else if (httpResponse.getStatusCode()
                .series() == HttpStatus.Series.CLIENT_ERROR) {
            // handle CLIENT_ERROR
            if (httpResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NoSuchElementException("Requested Resource Not Found.");
            }
            System.out.println("Body: " + httpResponse.getBody().available());
            if (httpResponse.getBody().available() <= 0)
                throw new HttpClientErrorException(httpResponse.getStatusCode(), httpResponse.getStatusText(),
                        httpResponse.getBody().readAllBytes(), Charset.defaultCharset());
        }
    }
}