import datetime
from django.utils.timezone import utc
from rest_framework.authentication import TokenAuthentication
from rest_framework import exceptions
from rest_framework.authtoken.models import Token
import pytz
from packages.models import TokenCounter

class ExpiringTokenAuthentication(TokenAuthentication):
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

        return (token.user, token)