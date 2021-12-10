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
from urllib.request import urlopen
import base64
from django.core.paginator import Paginator

#this commented out section is for specifically downloading the file
        #data = serializer.data
        #file_name = data['name']
        #content = data['content']
        #response = HttpResponse(content, content_type='application/zip')
        #response['Content-Disposition'] = 'attachment; filename=' + file_name
        #return response

# Create your views here.

#Potential Pagination
#@api_view(['POST'])
#def packages_list(request):
    
    #if request.method == 'POST':
        #list_packages = Package.objects.all() 
        
        #num = int(request.query_params['offset'])
        
        #paginator = Paginator(list_packages, 10) 
        
        #page_obj = paginator.get_page(num)
        #serialized_posts = [PackageSerializer(post) for post in page_obj]
        #return Response(serialized_posts.data, status=status.HTTP_200_OK)


#used for specific id operations
@api_view(['GET', 'DELETE', 'PUT'])
def package_element(request, id):
    try:
        post = Package.objects.get(metadata=id)
    except Package.DoesNotExist:
        return HttpResponse(status=400)

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
        package_data['metadata'].pop('ID')
        package_serializer = PackageSerializer(post, data=package_data) 
        if package_serializer.is_valid():
            package_serializer.save()
            return Response(status=status.HTTP_200_OK)
        return Response(status=status.HTTP_400_BAD_REQUEST)


#used for api package create call
@api_view(['POST'])
def package_create(request):
    
    returnData = {}

    if request.method == 'POST':
        request_data = request.data
        #section to check if package is hihgly rated enough
        
        #if package has URL then it needs to be ingested and scored
        if 'URL' in request_data['data']:
            url = request_data['data']['URL']
            
            r = requests.get('https://us-central1-ece461-repo-registry.cloudfunctions.net/function-final-scoring?' + url)

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
            
            #section used to retrieve the zip data from the given url
            http_response = urlopen(url + '/archive/refs/heads/master.zip')
            
            test1 = http_response.read()
            
            data = base64.b64encode(test1).decode('utf-8')
            
            request_data['data']['Content'] = data
        
        serializer = PackageSerializer(data=request_data)
        if serializer.is_valid():
            serializer.save()
            returnData['Name'] = request_data['metadata']['Name']
            returnData['Version'] = request_data['metadata']['Version']
            returnData['ID'] = request_data['metadata']['ID']
            return Response(returnData, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
 
@authentication_classes([])
@permission_classes([])   
@api_view(['PUT'])   
def testauthtoken(request):
    
    if request.method == 'PUT':
        if User.objects.filter(username=request.data['User']['name']).exists():
            
        
            user= User.objects.get(username=request.data['User']['name'])
       
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
        else:
            return Response(status=status.HTTP_401_UNAUTHORIZED)
    


class ObtainExpiringAuthToken(ObtainAuthToken):
    
    authentication_classes = []
    permission_classes = []
    
    def put(self, request):
        
        if User.objects.filter(username=request.data['User']['name']).exists():
            
        
            user= User.objects.get(username=request.data['User']['name'])
       
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
        else:
            return Response(status=status.HTTP_401_UNAUTHORIZED)

obtain_expiring_auth_token = ObtainExpiringAuthToken.as_view()

@api_view(['GET'])
def package_rate(request, id):

    try:
        post = Package.objects.get(metadata=id)
    except Package.DoesNotExist:
        return HttpResponse(status=404)

    if request.method == 'GET' and not post.data.URL == '':
        url = post.data.URL

        r = requests.get('https://us-central1-ece461-repo-registry.cloudfunctions.net/function-final-scoring?' + url)

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
    return Response(status=status.HTTP_400_BAD_REQUEST)

@api_view(['DELETE'])
def db_reset(request):

    if request.method == 'DELETE':

        packages = Package.objects.all()

        for package in packages:
            meta_data = metadata.objects.get(pk=package.metadata_id)
            data_data = data.objects.get(pk=package.data_id)
            package.delete()
            meta_data.delete()
            data_data.delete()
            
        if len(Package.objects.all()) == 0:
            return Response(status=status.HTTP_200_OK)