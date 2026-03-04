Gem::Specification.new do |s|
  s.name        = "mongo-keepalive"
  s.version     = "1.0.1"
  s.summary     = "Keep MongoDB Atlas free-tier clusters alive"
  s.description = "Periodically sends a ping command to prevent MongoDB Atlas free-tier clusters from becoming inactive."
  s.authors     = ["YadavSourabhGH"]
  s.email       = []
  s.homepage    = "https://github.com/YadavSourabhGH/mongo-keepalive"
  s.license     = "MIT"
  s.metadata    = {
    "bug_tracker_uri"   => "https://github.com/YadavSourabhGH/mongo-keepalive/issues",
    "changelog_uri"     => "https://github.com/YadavSourabhGH/mongo-keepalive/releases",
    "source_code_uri"   => "https://github.com/YadavSourabhGH/mongo-keepalive",
    "homepage_uri"      => "https://github.com/YadavSourabhGH/mongo-keepalive"
  }

  s.required_ruby_version = ">= 3.0"

  s.files = Dir["lib/**/*.rb", "README.md"]

  s.add_dependency "mongo", "~> 2.20"
end
