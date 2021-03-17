/*
 * Copyright (c) 2016-2026 Jumin Rubin
 * LinkedIn: https://www.linkedin.com/in/juminrubin/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jrtech.common.utils.validation;


import java.util.ArrayList;
import java.util.List;

public class ValidatorChain<T> implements Validator<T> {

    private List<Validator<T>> validatorList;
    
    protected List<ValidationItem<?, ? extends ValidationMessage>> validationResult;
    
    private final boolean breakOnError;
    
    public ValidatorChain() {
		this(false);
		validatorList = new ArrayList<>();
	}

    public ValidatorChain(boolean breakOnError) {
		this.breakOnError = breakOnError;
	}

    public List<Validator<T>> getValidatorList() {
        return validatorList;
    }
    
    public boolean isBreakOnError() {
		return breakOnError;
	}

    public void setValidatorList(List<Validator<T>> validatorList) {
        this.validatorList = validatorList;
    }
    
    public void validate(T object) {
        if (validationResult == null) {
            validatorList = new ArrayList<>();
        }
        validationResult.clear();
        for (Validator<T> validator : validatorList) {
			validator.validate(object);
			List<ValidationItem<?, ? extends ValidationMessage>> currentValidationResult = validator.getValidationResult();
			validationResult.addAll(currentValidationResult);
			if (breakOnError && currentValidationResult.size() > 0) {
			    break;
			}
		}
    }
    
    public List<ValidationItem<?, ? extends ValidationMessage>> getValidationResult() {
        return validationResult;
    }
    
}
