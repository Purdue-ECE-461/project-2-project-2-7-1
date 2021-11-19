from django.shortcuts import render
from django.http import HttpResponse, response
from rest_framework import status
from rest_framework.decorators import api_view
from rest_framework.response import Response
from packages.models import Package, metadata, data
from packages.serializers import PackageSerializer
from rest_framework.parsers import JSONParser
from django.contrib.auth.models import User
from rest_framework.authtoken.views import ObtainAuthToken
from rest_framework.authtoken.models import Token
from rest_framework.response import Response
import requests

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
        serializer = PackageSerializer(data=request_data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


@api_view(['PUT'])
def auth_put(request):
    token = Token.objects.get()
    user = User.get_username

    return Response(token.data, status=status.HTTP_200_OK)

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