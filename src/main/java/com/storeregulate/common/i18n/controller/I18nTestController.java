package com.storeregulate.common.i18n.controller;

import org.springframework.stereotype.Controller;

@Controller
public class I18nTestController {

	/*@Autowired
	private LocaleMessageSourceService messageSource;

	@ResponseBody
	@RequestMapping("i18n")
	public Map<String, Object> i18n() {
		Map<String, Object> result = new HashMap<>();
		result.put("system.lang", messageSource.getMessage("system.lang"));
		result.put("system.name", messageSource.getMessage("system.name"));
		result.put("system.welcome", messageSource.getMessage("system.welcome",new String[]{messageSource.getMessage("system.name")}));
		return result;
	}
	@ResponseBody
	@RequestMapping("ip")
	public String ip(HttpServletRequest request) {
		return "Your IP is " + IpAddressUtil.getClientRealIp(request);
	}
	@ResponseBody
	@RequestMapping("i18n/pretty-time")
	public Map<String, Object> prettyTime() {
		Map<String, Object> result = new HashMap<>();
		result.put("time", PrettyTimeUtil.format(new Date()));
		result.put("null", PrettyTimeUtil.format(null));
		result.put("null", PrettyTimeUtil.format(DateTime.now().minusYears(5000).toDate()));
		return result;
	}


	@ResponseBody
	@RequestMapping("i18n/change")
	public String changeSessionLanauage(HttpServletRequest request, HttpServletResponse response, String lang) {
		System.out.println(lang);
		String language = "当前语言是中文";
		LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
		if ("zh".equals(lang)) {
			localeResolver.setLocale(request, response, new Locale("zh", "CN"));
		} else if ("en".equals(lang)) {
			localeResolver.setLocale(request, response, new Locale("en", "US"));
			language = "current language is English";
		}
		return language;
	}
	@ResponseBody
	@RequestMapping("i18n/ex")
	public String ex(HttpServletRequest request, HttpServletResponse response, String lang) {
		// 通过占位符形式构造
		// throw new BusinessException("system.welcome", new String[]{messageSource.getMessage("system.name")});
		// 通过国际化key构造
		//throw new BusinessException("system.name");
		// 通过普通字符串构造
	    //throw new BusinessException("简单异常测试");
		//throw new RuntimeException("简单异常测试");
		throw new SimpleException("system.welcome");
		
	}*/
}
