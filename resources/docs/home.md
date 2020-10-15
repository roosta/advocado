# Congratulations, you've successfully authenticated

Welcome to **Advocado**, more content will be displayed here when ready

## Why are you seeing this page?

This page is a markdown file loaded from the backend when a request is sent with an authentication header, its here simply to demonstrate that the route works properly.

Currently the home routes looks like this:

```clojure
(defn home-routes
  "These routes are when user is authenticated using auth-token middleware, the
  only exception is the root route, which returns our SPA so that shouldn't be
  wrapped in auth-token"
  []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/docs" {:middleware [middleware/wrap-auth-token]
             :get (fn [_]
                    (-> (response/ok (-> "docs/home.md" io/resource slurp))
                        (response/header "Content-Type" "text/plain; charset=utf-8")))}]])
```

Where `wrap-auth-token ensures` that for any request to the `/docs` route, a request header must be included. It is handled in frontend by the namespace `advocado.ajax`:

```clojure
(defn default-headers [request]
  (let [token (<sub [:auth/current-token])]
    (if (local-uri? request)
      (-> request
          (update :headers #(merge {"x-csrf-token" js/csrfToken
                                    "x-token" token} %)))
      request)))
```

And is loaded as an interceptor here:

```clojure
(defn load-interceptors! []
  (swap! ajax/default-interceptors
         conj
         (ajax/to-interceptor {:name "default headers"
                               :request default-headers})))
```
