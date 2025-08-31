package com.menon;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CredentialRepository extends MongoRepository<Credential, String> {
    List<Credential> findByType(String type);
}
