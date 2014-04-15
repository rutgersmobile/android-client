package edu.rutgers.css.Rutgers;

import java.util.ArrayList;
import java.util.List;

import org.jdeferred.Deferred;
import org.jdeferred.DeferredManager;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DefaultDeferredManager;
import org.jdeferred.impl.DeferredObject;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneReject;
import org.jdeferred.multiple.OneResult;
import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;

// Singleton class that provides access to the nextbus api. Uses the json that nextbusjs generates to create requests
// against the official nextbus api.
public class Nextbus {

	private static boolean isSetup = false;
	private static Promise<Object, Object, Object> configured;
	private static final String TAG = "Nextbus";
	
	private static JSONObject mNBConf;
	private static JSONObject mNWKConf;
	
	private static JSONObject mNBActive;
	private static JSONObject mNWKActive;
	
	private static final String BASE_URL = "http://webservices.nextbus.com/service/publicXMLFeed?command=";
	
	private static void setup () {
		if (!isSetup) {
			isSetup = true;
			
			// This promise is used to notify the other objects that the object has been configured.
			final Deferred confd = new DeferredObject<Object, Object, Object>();
			configured = confd.promise();
			
			final Promise promiseNBActive = Request.api("bus/active/nb");
			final Promise promiseNWKActive = Request.api("bus/active/nwk");
			final Promise promiseNBConf = Request.api("bus/config/nb");
			final Promise promiseNWKConf = Request.api("bus/config/nwk");
			
			DeferredManager dm = new DefaultDeferredManager();
			
			dm.when(promiseNBActive, promiseNBConf, promiseNWKActive, promiseNWKConf).done(new DoneCallback<MultipleResults>() {
				@Override
				public void onDone(MultipleResults res) {
					
					try {
					
						for (int i = 0; i < res.size(); i++) {
							OneResult r = res.get(i);
							
							if (r.getPromise() == promiseNBActive) mNBActive = (JSONObject) r.getResult();
							else if (r.getPromise() == promiseNWKActive) mNWKActive = (JSONObject) r.getResult();
							else if (r.getPromise() == promiseNBConf) mNBConf = (JSONObject) r.getResult();
							else if (r.getPromise() == promiseNWKConf) mNWKConf = (JSONObject) r.getResult();
						}
			
						confd.resolve(null);
					} catch (Exception e) {
						Log.d(TAG, Log.getStackTraceString(e));
						confd.reject(e);
					}
				}
			}).fail(new FailCallback<OneReject>() {
				@Override
				public void onFail(OneReject reject) {
					
					AjaxStatus r = (AjaxStatus) reject.getReject();
					
					Log.d(TAG, "stuff failed: " + r.getCode());
				}
			});
		}
	}
	
	public static Promise<ArrayList, Exception, Double> routePredict (final String agency, final String route) {
		final Deferred<ArrayList, Exception, Double> d = new DeferredObject<ArrayList, Exception, Double>();
		setup();
		
		configured.then(new DoneCallback<Object>() {
			public void onDone(Object o) {
				Log.d("Nextbus", "We're configured and returning results");
				JSONObject conf = agency.equals("nb") ? mNBConf : mNWKConf;
				
				String queryString = BASE_URL + "predictionsForMultiStops&a=" + (agency.equals("nb")? "rutgers" : "rutgers-newark");
				
				try {
				
					JSONObject routeData = conf.getJSONObject("routes").getJSONObject(route);
					JSONArray stops = routeData.getJSONArray("stops");
					for (int i = 0; i < stops.length(); i++) {
						queryString += "&stops=" + route + "|null|" + stops.getString(i);
					}
									
					Request.xml(queryString).done(new DoneCallback<XmlDom>() {
						@Override
						public void onDone(XmlDom xml) {
							ArrayList<Prediction> results = new ArrayList<Prediction>();
							
							List<XmlDom> entries = xml.tags("predictions");

							for (int i = 0; i < entries.size(); i++) {
								XmlDom p = entries.get(i);
								
								Prediction oneresult = new Prediction(p.attr("stopTitle"), p.attr("stopTag"));
								
								List<XmlDom> xmlpredictions = p.tags("prediction");
								
								for (int j = 0; j < xmlpredictions.size(); j++) {
									oneresult.addPrediction(Integer.parseInt(xmlpredictions.get(j).attr("minutes")));
								}
								results.add(oneresult);
							}
							
							d.resolve(results);
						}
					}).fail(new FailCallback<AjaxStatus>() {
						@Override
						public void onFail(AjaxStatus e) {
							d.reject(new Exception(e.toString()));
						}
					});
	
				} catch (Exception e) {
					d.reject(e);
				}

			}
		});
		
		return d.promise();
	}
	
	public static Promise<ArrayList, Exception, Double> stopPredict (final String agency, final String stop) {
		final Deferred<ArrayList, Exception, Double> d = new DeferredObject<ArrayList, Exception, Double>();
		setup();
		
		configured.then(new DoneCallback<Object>() {
			public void onDone(Object o) {
				Log.d("Nextbus", "We're configured and returning results");
				JSONObject conf = agency.equals("nb") ? mNBConf : mNWKConf;
				
				String queryString = BASE_URL + "predictionsForMultiStops&a=" + (agency.equals("nb")? "rutgers" : "rutgers-newark");
				
				try {
				
					JSONObject routeData = conf.getJSONObject("stopsByTitle").getJSONObject(stop);
					JSONArray tags = routeData.getJSONArray("tags");
					for (int i = 0; i < tags.length(); i++) {
						JSONArray routes = conf.getJSONObject("stops").getJSONObject(tags.getString(i)).getJSONArray("routes");
						for (int j = 0; j < routes.length(); j++)
							queryString += "&stops=" + routes.getString(j) + "|null|" + tags.getString(i);
					}
					
					Request.xml(queryString).done(new DoneCallback<XmlDom>() {
						@Override
						public void onDone(XmlDom xml) {
							ArrayList<Prediction> results = new ArrayList<Prediction>();
							
							List<XmlDom> entries = xml.tags("predictions");

							for (int i = 0; i < entries.size(); i++) {
								XmlDom p = entries.get(i);
								
								Prediction oneresult = new Prediction(p.attr("routeTitle"), p.attr("routeTag"));
								
								XmlDom dirtag = p.tag("direction");
								// If there's no dirtag then there are no predictions. Predictions always appear as children of a 'direction' tag
								if (dirtag != null) oneresult.setDirection(dirtag.attr("title"));
								else continue;

								List<XmlDom> xmlpredictions = p.tags("prediction");
								
								for (int j = 0; j < xmlpredictions.size(); j++) {
									oneresult.addPrediction(Integer.parseInt(xmlpredictions.get(j).attr("minutes")));
								}
								results.add(oneresult);
							}
							
							d.resolve(results);
						}
					}).fail(new FailCallback<AjaxStatus>() {
						@Override
						public void onFail(AjaxStatus e) {
							d.reject(new Exception(e.toString()));
						}
					});
	
				} catch (Exception e) {
					d.reject(e);
				}

			}
		});
		
		return d.promise();
	}
	
	public static Promise<JSONArray, Exception, Double> getRoutes (final String agency) {
		final Deferred<JSONArray, Exception, Double> d = new DeferredObject<JSONArray, Exception, Double>();
		setup();
		
		configured.then(new DoneCallback<Object>() {
			public void onDone(Object o) {
				try {
					JSONObject conf = agency.equals("nb") ? mNBConf : mNWKConf;
					JSONObject active = agency.equals("nb") ? mNBActive : mNWKActive;
					
					JSONArray routes;
					if (active != null) routes = active.getJSONArray("routes");
					else routes = conf.getJSONArray("sortedRoutes");
					
					d.resolve(routes);
				} catch (Exception e) {
					d.reject(e);
				}
			}
		});
		
		return d;
	}
	
	public static Promise<JSONArray, Exception, Double> getStops (final String agency) {
		final Deferred<JSONArray, Exception, Double> d = new DeferredObject<JSONArray, Exception, Double>();
		setup();
		
		configured.then(new DoneCallback<Object>() {
			public void onDone(Object o) {
				try {
					JSONObject conf = agency.equals("nb") ? mNBConf : mNWKConf;
					JSONObject active = agency.equals("nb") ? mNBActive : mNWKActive;
					
					JSONArray stops;
					if (active != null) stops = active.getJSONArray("stops");
					else stops = conf.getJSONArray("sortedStops");
					
					d.resolve(stops);
				} catch (Exception e) {
					d.reject(e);
				}
			}
		});
		
		return d;
	}
}
