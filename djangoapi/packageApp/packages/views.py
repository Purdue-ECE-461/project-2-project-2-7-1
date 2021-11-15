from django.shortcuts import render
from django.http import HttpResponse
from rest_framework import status
from rest_framework.decorators import api_view
from rest_framework.response import Response
from packages.models import Package, metadata, data
from packages.serializers import PackageSerializer
from rest_framework.parsers import JSONParser

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
        return Response(serializer.data)
    elif request.method == 'DELETE':
        serializer = PackageSerializer(post)
    elif request.method == 'PUT':
        package_data = JSONParser().parse(request)
        package_serializer = PackageSerializer(post, data=package_data) 
        if package_serializer.is_valid():
            package_serializer.save()
            return Response(package_serializer.data, status=status.HTTP_201_CREATED)
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
