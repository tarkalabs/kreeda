;; migrations/20140927130732232-add_provider_meta.clj

(defn up []
  ["create extension hstore", "alter table identities add column provider_meta hstore"])

(defn down []
  ["alter table identities drop column provider_meta"])
