package com.menon;

import io.github.resilience4j.retry.RetryConfig;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("customer/v1")
public class MainRestController
{
    private static final Logger logger = LoggerFactory.getLogger(MainRestController.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    CredentialRepository credentialRepository;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    private RetryConfig retryConfig;

    @PostMapping("signup")
    public ResponseEntity<String> signup(@RequestBody Credential credential)
    {
        credentialRepository.save(credential);
        return ResponseEntity.ok("Customer Registered successfully!");
    }

    @GetMapping("login")
    public ResponseEntity<?> login(@RequestBody CredentialAuthView credentialAuthView)
    {

          if(credentialRepository.findById(credentialAuthView.getPhone()).isPresent())
          {
              Credential credential = credentialRepository.findById(credentialAuthView.getPhone()).get();
              if(credential.getPassword().equals(credentialAuthView.getPassword()))
              {
                  // Generate a token and save it to the database
                  Token token = getUserToken(credentialAuthView);
                  return ResponseEntity.ok().header("Authorization", token.getTokenValue()).
                          body("Login Successful");
              }
              else
              {
                  return  ResponseEntity.ok("Please enter valid credentials | Incorrect PASSWORD");
              }
          }
          else
          {
              return  ResponseEntity.ok("Login Failed | User with this PHONE NUMBER does not exist");
          }
    }

    @GetMapping("validate") // AUTHENTICATION OF REQUEST RETURNS A VALIDATED PRINCIPAL OBJECT
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String tokenValue)
    { // AOP - MICROMETER
        // Aspected Oriented Programming - Micrometer does its job here [ trace + span generation

        logger.info("Request received to validate token: " + tokenValue);
        Optional<Token> token = tokenRepository.findById(tokenValue);
        if (token.isPresent() && token.get().getStatus().equals("ACTIVE"))
        {
            Optional<Credential> credential = credentialRepository.findById(token.get().getPhone());
            if(credential.isPresent()){
                logger.info("Token validated successfully");
                //make INACTIVE if current time > generation time + expirytime
                Principal principal = new Principal();
                principal.setState("VALID");
                principal.setUsername(token.get().getPhone());
                principal.setType(credential.get().getType());
                logger.info("Principal validated successfully");
                return ResponseEntity.ok(principal);
            }
            else{
                return getPrincipalResponseEntityForInvalidToken();
            }
        }
        else {
            return getPrincipalResponseEntityForInvalidToken();

        }
    }

    @NotNull
    private static ResponseEntity<Principal> getPrincipalResponseEntityForInvalidToken() {
        logger.info("Token checked to be invalid or expired");
        Principal principal = new Principal();
        principal.setState("INVALID");
        principal.setUsername(null);
        logger.info("Principal invalid or expired");
        return ResponseEntity.ok(principal);
    }

    @GetMapping("get/users/{type}")
    public ResponseEntity<List<String>> getUsersOfType(@PathVariable("type") String type)
    {
        logger.info("Request received to get users of type " + type);

        String redisKey = "CUSTOM_LIST_" + type;
        List<String> userList = new ArrayList<>();

        // check for the data in the REDIS CACHE first
        if (redisTemplate.hasKey(redisKey) && redisTemplate.opsForList().size(redisKey) > 0)
        {
            logger.info("Getting users of type " + type + " from Redis cache");
            List<Object> cachedUserList = (List<Object>) redisTemplate.opsForList().range(redisKey, 0, -1);
            userList = cachedUserList.stream().map(Object::toString).toList();
        }
        else
        {
            logger.info("Getting users of type " + type + " from the Database and putting it in Redis cache");
            // if the cache is empty, fetch from the database
            List<Credential> credentialList =  credentialRepository.findByType(type);
            userList =  credentialList.stream().map(Credential::getPhone).toList();
            userList.forEach(user -> redisTemplate.opsForList().leftPush(redisKey, user));
        }

        return ResponseEntity.ok(userList);
    }

    private Token getUserToken(CredentialAuthView credentialAuthView) {
        Token token = new Token();

        token.setTokenValue(String.valueOf(new Random().nextInt(1000000)));
        token.setPhone(credentialAuthView.getPhone());
        token.setStatus("ACTIVE");
        token.setCreatedAt(Instant.now());
        token.setExpiresIn(3600); // Token valid for 1 hour
        tokenRepository.save(token);
        return token;
    }

}
