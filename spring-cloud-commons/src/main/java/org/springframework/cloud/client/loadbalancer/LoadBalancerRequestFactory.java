/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.cloud.client.loadbalancer;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.util.List;

/**
 * 负载均衡请求工厂，用来创建一个负载均衡请求
 * Creates {@link LoadBalancerRequest}s for {@link LoadBalancerInterceptor} and
 * {@link RetryLoadBalancerInterceptor}. Applies {@link LoadBalancerRequestTransformer}s
 * to the intercepted {@link HttpRequest}.
 *
 * @author William Tran
 */
public class LoadBalancerRequestFactory {

	private final LoadBalancerClient loadBalancer;

	private List<LoadBalancerRequestTransformer> transformers;

	public LoadBalancerRequestFactory(LoadBalancerClient loadBalancer,
									  List<LoadBalancerRequestTransformer> transformers) {
		this.loadBalancer = loadBalancer;
		this.transformers = transformers;
	}

	public LoadBalancerRequestFactory(LoadBalancerClient loadBalancer) {
		this.loadBalancer = loadBalancer;
	}

	public LoadBalancerRequest<ClientHttpResponse> createRequest(final HttpRequest request, final byte[] body,
																 final ClientHttpRequestExecution execution) {
		return instance -> {
			HttpRequest serviceRequest = new ServiceRequestWrapper(request, instance, this.loadBalancer);
			if (this.transformers != null) {
				for (LoadBalancerRequestTransformer transformer : this.transformers) {
					serviceRequest = transformer.transformRequest(serviceRequest, instance);
				}
			}
			return execution.execute(serviceRequest, body);
		};
	}

}
