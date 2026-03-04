Gem::Specification.new do |s|
  s.name        = "mongo-keepalive"
  s.version     = "1.0.0"
  s.summary     = "Keep MongoDB Atlas free-tier clusters alive"
  s.description = "Periodically sends a ping command to prevent MongoDB Atlas free-tier clusters from becoming inactive."
  s.authors     = ["mongo-keepalive contributors"]
  s.homepage    = "https://github.com/YadavSourabhGH/mongo-keepalive"
  s.license     = "MIT"

  s.required_ruby_version = ">= 3.0"

  s.files = Dir["lib/**/*.rb"]

  s.add_dependency "mongo", "~> 2.20"
end
