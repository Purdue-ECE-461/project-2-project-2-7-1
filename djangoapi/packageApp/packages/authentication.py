import datetime
from django.utils.timezone import utc
from rest_framework import authentication
from rest_framework.authentication import BaseAuthentication
from rest_framework import exceptions
from rest_framework.authtoken.models import Token
import pytz
from packages.models import TokenCounter
from django.contrib.auth.models import User

class ExampleAuthentication(BaseAuthentication):
    def authenticate(self, request):
        path = request.META.get('PATH_INFO')
        
        token_string = request.META.get('HTTP_X_AUTHORIZATION') # get the username request header
        
        if token_string == None and path == '/authenticate':
            try:
                user = User.objects.get(username=request.data['User']['name'])
            except User.DoesNotExist:
                raise exceptions.AuthenticationFailed('Invalid credentials')
                
            token = Token.objects.get(user_id=user.id)
            return(token.user, token)
        else:
        
            keyword = token_string.split(' ', 1)[0]
            token = token_string.split(' ', 1)[1]
        
            if keyword != 'bearer':
                raise exceptions.AuthenticationFailed('')
        
        utc = pytz.UTC

        try:
            token = Token.objects.get(key=token)
        except Token.DoesNotExist:
            raise exceptions.AuthenticationFailed('Invalid token')

        if not token.user.is_active:
            raise exceptions.AuthenticationFailed('User inactive or deleted')

        utc_now = datetime.datetime.utcnow()
        
        created_token = token.created.replace(tzinfo=utc)
        
        limit = utc_now - datetime.timedelta(hours=24)
        
        token_limit = limit.replace(tzinfo=utc)
        
        try:
            counter = TokenCounter.objects.get(token_id=token.user_id)
        except TokenCounter.DoesNotExist:
            raise exceptions.AuthenticationFailed('Invalid counter')

        if created_token < token_limit:
            raise exceptions.AuthenticationFailed('Token has expired')
        
        if counter.token_count >= counter.token_hitlimit:
            raise exceptions.AuthenticationFailed('Token has expired')
        
        counter.token_count += 1
        counter.save()

        return (token.user, token)
        
#old auhtorization code used 
""" class ExpiringTokenAuthentication(TokenAuthentication):
    
    keyword = 'bearer'
    
    def authenticate_credentials(self, key):
        
        utc = pytz.UTC

        try:
            token = Token.objects.get(key=key)
        except Token.DoesNotExist:
            raise exceptions.AuthenticationFailed('Invalid token')

        if not token.user.is_active:
            raise exceptions.AuthenticationFailed('User inactive or deleted')

        utc_now = datetime.datetime.utcnow()
        
        created_token = token.created.replace(tzinfo=utc)
        
        limit = utc_now - datetime.timedelta(hours=24)
        
        token_limit = limit.replace(tzinfo=utc)
        
        try:
            counter = TokenCounter.objects.get(token_id=token.user_id)
        except TokenCounter.DoesNotExist:
            raise exceptions.AuthenticationFailed('Invalid counter')

        if created_token < token_limit:
            raise exceptions.AuthenticationFailed('Token has expired')
        
        if counter.token_count >= counter.token_hitlimit:
            raise exceptions.AuthenticationFailed('Token has expired')
        
        counter.token_count += 1
        counter.save()

        return (token.user, token) """