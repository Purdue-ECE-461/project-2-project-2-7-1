import datetime
from django.shortcuts import render
from django.http import HttpResponse, response
from rest_framework import status
from rest_framework.decorators import api_view, authentication_classes, permission_classes
from rest_framework.response import Response
from packages.models import Package, metadata, data
from packages.serializers import PackageSerializer
from rest_framework.parsers import JSONParser
from django.contrib.auth.models import User
from rest_framework.authtoken.views import ObtainAuthToken
from rest_framework.authtoken.models import Token
from rest_framework.response import Response
from django.contrib.auth.models import User
import requests
import pytz
from packages.models import TokenCounter

#this commented out section is for specifically downloading the file
        #data = serializer.data
        #file_name = data['name']
        #content = data['content']
        #response = HttpResponse(content, content_type='application/zip')
        #response['Content-Disposition'] = 'attachment; filename=' + file_name
        #return response

# Create your views here.


#used for specific id operations
@api_view(['GET', 'DELETE', 'PUT'])
def package_element(request, id):
    try:
        post = Package.objects.get(metadata=id)
    except Package.DoesNotExist:
        return HttpResponse(status=404)

    if request.method == 'GET':
        serializer = PackageSerializer(post)
        return Response(serializer.data, status=status.HTTP_200_OK)
    elif request.method == 'DELETE':
        meta_data = metadata.objects.get(pk=id)
        data_data = data.objects.get(pk=post.data_id)
        post.delete()
        meta_data.delete()
        data_data.delete()
        return Response(status=status.HTTP_200_OK)
    elif request.method == 'PUT':
        package_data = JSONParser().parse(request)
        package_serializer = PackageSerializer(post, data=package_data) 
        if package_serializer.is_valid():
            package_serializer.save()
            return Response(package_serializer.data, status=status.HTTP_200_OK)
        return Response(package_serializer.errors, status=status.HTTP_400_BAD_REQUEST)


#used for api package create call
@api_view(['POST'])
def package_create(request):

    if request.method == 'POST':
        request_data = request.data
        #section to check if package is hihgly rated enough
        
        url = request_data['data']['URL']

        r = requests.get('https://us-central1-ece461-repo-registry.cloudfunctions.net/module-eval-oh-function?' + url)

        data = r.content.decode("utf-8")
        scores = data.split(' ')

        scores = {
            "Total Score": scores[1],
            "RampUp": scores[2],
            "Correctness": scores[3],
            "BusFactor": scores[4],
            "ResponsiveMaintainer": scores[5],
            "LicenseScore": scores[6],
            "Dependency": scores[7]
            }
        
        if float(scores['Total Score']) < .5:
            return Response('Package did not recieve score above .5', status=status.HTTP_400_BAD_REQUEST)
        
        serializer = PackageSerializer(data=request_data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class ObtainExpiringAuthToken(ObtainAuthToken):
    def put(self, request):
        
        user, created = User.objects.get_or_create(username=request.data['User']['name'])
        
        token, created = Token.objects.get_or_create(user_id=user.id)
        
        counter, created = TokenCounter.objects.get_or_create(token_id=token.user_id, token_hitlimit=1000)

        utc_now = datetime.datetime.utcnow() 
        utc = pytz.UTC
        
        created_token = token.created.replace(tzinfo=utc)
        
        limit = utc_now - datetime.timedelta(hours=24)
        
        token_limit = limit.replace(tzinfo=utc)   
        if not created and created_token < token_limit:
            token.delete()
            token = Token.objects.create(user_id=user.id)
            token.created = datetime.datetime.utcnow()
            token.save()
            
        if not created and counter.token_count >= counter.token_hitlimit:
            counter.token_count = 0
            counter.save()
            token.delete()
            token = Token.objects.create(user_id=user.id)
            token.created = datetime.datetime.utcnow()
            token.save()
            
        return Response('bearer ' + token.key, status=status.HTTP_200_OK)

obtain_expiring_auth_token = ObtainExpiringAuthToken.as_view()

@api_view(['GET'])
def package_rate(request, id):

    try:
        post = Package.objects.get(metadata=id)
    except Package.DoesNotExist:
        return HttpResponse(status=404)

    if request.method == 'GET':
        url = post.data.URL

        r = requests.get('https://us-central1-ece461-repo-registry.cloudfunctions.net/module-eval-oh-function?' + url)

        data = r.content.decode("utf-8")
        scores = data.split(' ')

        scores = {
            "RampUp": scores[1],
            "Correctness": scores[2],
            "BusFactor": scores[3],
            "ResponsiveMaintainer": scores[4],
            "LicenseScore": scores[5],
            "GoodPinningPractice": scores[6]
            }

        return Response(scores, status=status.HTTP_200_OK)

@api_view(['DELETE'])
def db_reset(request):

    if request.method == 'DELETE':

        packages = Package.objects.all()

        for package in packages:
            meta_data = metadata.objects.get(pk=package.id)
            data_data = data.objects.get(pk=package.data_id)
            package.delete()
            meta_data.delete()
            data_data.delete()