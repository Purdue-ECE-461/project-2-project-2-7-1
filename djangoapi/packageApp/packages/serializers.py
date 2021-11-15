from django.db.models import fields
from rest_framework import serializers
from .models import Package, data, metadata


class BinarySerializerField(serializers.Field):
    def to_representation(self, value):
        if isinstance(value, memoryview):
            test = bytes(value)
            return test
        else:
            return value.decode('utf-8')

    def to_internal_value(self, data):
        return str.encode(data)

class DataSerializer(serializers.ModelSerializer):
    Content = BinarySerializerField()

    def get_Content(self, obj):
        test = bytes(obj.Content)
        return test
    
    class Meta:
        model = data
        fields = ('Content', 'URL', 'JSProgram')
        
class MetaDataSerializer(serializers.ModelSerializer):
    
    class Meta:
        model = metadata
        fields = ('Name', 'Version', 'ID')

class PackageSerializer(serializers.ModelSerializer):

    metadata = MetaDataSerializer()
    data = DataSerializer()

    class Meta:
        model = Package
        fields = ('metadata', 'data')
        
    def create(self, validated_data):
        """
        Overriding the default create method of the Model serializer.
        :param validated_data: data containing all the details of student
        :return: returns a successfully created student record
        """
        data_data = validated_data.pop('data')
        data_ser = DataSerializer.create(DataSerializer(), validated_data=data_data)
        metadata_data = validated_data.pop('metadata')
        metadata_ser = MetaDataSerializer.create(MetaDataSerializer(), validated_data=metadata_data)
        package, created = Package.objects.update_or_create(data=data_ser, metadata=metadata_ser)
        return package