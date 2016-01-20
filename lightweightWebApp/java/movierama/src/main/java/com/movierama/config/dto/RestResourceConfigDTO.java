package com.movierama.config.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.movierama.config.Constants;
import com.movierama.connectors.integration.movies.MoviesConnector;
import com.movierama.connectors.integration.movies.impl.ProviderType;
import com.movierama.rest.dto.QueryType;
import com.movierama.rest.dto.RestParam;
import com.movierama.util.Var;
import com.movierama.util.io.JsonIO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Builder;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE, include = JsonTypeInfo.As.PROPERTY)
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestResourceConfigDTO {

	@JsonTypeInfo(use = JsonTypeInfo.Id.NONE, include = JsonTypeInfo.As.PROPERTY)
	@Data
	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class RestResourceParamConfigDTO {
		String name;
		String value;

		public RestResourceParamConfigDTO copy() {
			return new RestResourceParamConfigDTO(name, value);
		}

		public static List<RestResourceParamConfigDTO> copy(List<RestResourceParamConfigDTO> params) {
			List<RestResourceParamConfigDTO> params_ = new ArrayList<>();
			for (RestResourceParamConfigDTO param : params) {
				params_.add(param.copy());
			}
			return params_;
		}
	};

	ProviderType providerType;
	QueryType queryType;
	String resourceKey;
	String urlTemplate;
	List<RestResourceParamConfigDTO> params = new ArrayList<>();

	public RestResourceParamConfigDTO paramOf(String name) {
		for (RestResourceParamConfigDTO param : this.params) {
			if (name.equals(param.getValue())) {
				return param;
			}
		}
		return null;
	}

	private static boolean isDynamicParam(String param) {
		return RestParam.forValue(param) == RestParam.DYNAMIC;
	}
	public String toURLString(String... args) {
		if (args.length % 2 != 0)
			throw new RuntimeException("Bad args: " + Var.toList(args));
		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < args.length; i += 2) {
			String val = isDynamicParam(args[i])?args[i+1]:Var.encodeURI(args[i + 1]);
			map.put(args[i], val);
		}
//		if(Constants.DEV_MODE) {
//			Constants.LOGGER.info("-->toURLString args = " + map);
//		}
		return toURLString(map);
	}

	public String toURLString(Map<String, String> map) {
		for (RestResourceParamConfigDTO param : this.params) {
			if (StringUtils.isNotEmpty(param.getValue())) {
				String val = isDynamicParam(param.getName())?param.getValue():Var.encodeURI(param.getValue());
				map.put(param.getName(), val);
			}
		}
		String retVal = Var.substParamsOfTemplate(this.urlTemplate, map);
		return retVal;
	}

	public String toURLString() {
		String retVal = toURLString(new HashMap<String, String>());
		return retVal;
	}

	public RestResourceConfigDTO copy() {
		List<RestResourceParamConfigDTO> params_ = new ArrayList<>();
		for (RestResourceParamConfigDTO param : params) {
			params_.add(param.copy());
		}
		return new RestResourceConfigDTO(providerType, queryType, resourceKey, urlTemplate, params_);
	}

	public static RestResourceConfigDTO lookup(List<RestResourceConfigDTO> apis, ProviderType providerType,
			QueryType queryType) {
		for (RestResourceConfigDTO api : apis) {
			if (api.getProviderType() == providerType && queryType == api.getQueryType()) {
				return api;
			}
		}
		return null;
	}
}
