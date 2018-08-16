# -*- coding: utf-8 -*-

from flask import request, current_app


class CORSify(object):
    """Provides CORS support"""

    def __init__(self, app=None):
        if app is not None:
            self.init_app(app)

    def init_app(self, app):
        """Configures the configured Flask app to enforce SSL."""

        app.before_request(self.respond_to_options_request)
        app.after_request(self.add_cors_headers)

    def respond_to_options_request(self):
        """Reply 200 on OPTIONS request for selected origins
        """
        if request.method == 'OPTIONS':
            request_origin = request.headers.get('Origin', None)
            if request_origin not in current_app.config.get('CORS_ALLOWED_ORIGINS', []):
                return

            response = current_app.make_default_options_response()
            response.headers.add('Access-Control-Allow-Origin', request_origin)
            response.headers.add('Access-Control-Allow-Credentials', 'true')
            response.headers.add('Access-Control-Allow-Headers', 'Content-Type')
            response.headers.add('Access-Control-Allow-Headers', 'Origin')
            response.headers.add('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')

            return response

    def add_cors_headers(self, response):
        """append CORS headers to selected origins
        """
        if request.method == 'OPTIONS':
            return response

        request_origin = request.headers.get('Origin', None)
        if request_origin in current_app.config.get('CORS_ALLOWED_ORIGINS', []):
            response.headers.add('Access-Control-Allow-Origin', request_origin)
            response.headers.add('Access-Control-Allow-Credentials', 'true')
            response.headers.add('Access-Control-Allow-Headers', 'Content-Type')
            response.headers.add('Access-Control-Allow-Headers', 'Origin')
            response.headers.add('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')

        return response
