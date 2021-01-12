/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.sample.bookstore.servicebroker.service;

import org.springframework.cloud.sample.bookstore.servicebroker.model.ServiceInstance;
import org.springframework.cloud.sample.bookstore.servicebroker.repository.ServiceInstanceRepository;
import org.springframework.cloud.sample.bookstore.web.service.BookStoreService;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerAsyncRequiredException;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerInvalidParametersException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceUpdateNotSupportedException;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceResponse.CreateServiceInstanceResponseBuilder;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.instance.GetServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.GetServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.instance.UpdateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.UpdateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.stereotype.Service;

import java.security.Policy.Parameters;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class BookStoreServiceInstanceService implements ServiceInstanceService {
	private final BookStoreService storeService;
	private final ServiceInstanceRepository instanceRepository;

	public BookStoreServiceInstanceService(BookStoreService storeService, ServiceInstanceRepository instanceRepository) {
		this.storeService = storeService;
		this.instanceRepository = instanceRepository;
	}

	@Override
	public CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest request) {
		String instanceId = request.getServiceInstanceId();

		CreateServiceInstanceResponseBuilder responseBuilder = CreateServiceInstanceResponse.builder();

		if (instanceRepository.existsById(instanceId)) {
			responseBuilder.instanceExisted(true);
		} else {
			storeService.createBookStore(instanceId);

			saveInstance(request, instanceId);
		}

		return responseBuilder.build();
	}

	@Override
	public GetServiceInstanceResponse getServiceInstance(GetServiceInstanceRequest request) {
		String instanceId = request.getServiceInstanceId();

		Optional<ServiceInstance> serviceInstance = instanceRepository.findById(instanceId);

		if (serviceInstance.isPresent()) {
			return GetServiceInstanceResponse.builder()
					.serviceDefinitionId(serviceInstance.get().getServiceDefinitionId())
					.planId(serviceInstance.get().getPlanId())
					.parameters(serviceInstance.get().getParameters())
					.build();
		} else {
			throw new ServiceInstanceDoesNotExistException(instanceId);
		}
	}

	@Override
	public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request) {
		String instanceId = request.getServiceInstanceId();

		if (instanceRepository.existsById(instanceId)) {
			storeService.deleteBookStore(instanceId);
			instanceRepository.deleteById(instanceId);

			return DeleteServiceInstanceResponse.builder().build();
		} else {
			throw new ServiceInstanceDoesNotExistException(instanceId);
		}
	}

	private void saveInstance(CreateServiceInstanceRequest request, String instanceId) {
		ServiceInstance serviceInstance = new ServiceInstance(instanceId, request.getServiceDefinitionId(),
				request.getPlanId(), request.getParameters());
		instanceRepository.save(serviceInstance);
		storeService.createBookStore(instanceId);
	}
	
	/**
	 * Update a service instance.
	 *
	 * @param request containing the details of the request
	 * @return an {@link UpdateServiceInstanceResponse} on successful processing of the request
	 * @throws ServiceInstanceUpdateNotSupportedException if particular change is not supported
	 *         or if the request can not currently be fulfilled due to the state of the instance
	 * @throws ServiceInstanceDoesNotExistException if a service instance with the given ID is not known to the broker
	 * @throws ServiceBrokerAsyncRequiredException if the broker requires asynchronous processing of the request
	 * @throws ServiceBrokerInvalidParametersException if any parameters passed in the request are invalid
	 */
	@Override
	public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest request) {
		String planid = request.getPlanId();
		System.out.println("planid = " + planid);
		Map<String, Object> parameters = request.getParameters();
		Set<String> keys = parameters.keySet();
		Collection<Object> values = parameters.values();
		throw new UnsupportedOperationException("This service broker does not support updating service instances. " +
				"The service broker should set 'plan_updateable:false' in the service catalog, " +
				"or provide an implementation of the update instance API.");
	}
}
