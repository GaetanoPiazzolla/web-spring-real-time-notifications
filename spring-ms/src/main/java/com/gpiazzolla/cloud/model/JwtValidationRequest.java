package com.gpiazzolla.cloud.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class JwtValidationRequest implements Serializable {

	private static final long serialVersionUID = 3074053963691015640L;

	private String token;

	private String applicationName;

	private String operationName;

}