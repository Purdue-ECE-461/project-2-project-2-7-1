from django.db import models
from django.db.models.fields import BinaryField

# Create your models here.


class data(models.Model):
    Content = models.BinaryField(default=b'')
    URL = models.CharField(max_length=70, default='')
    JSProgram = models.TextField()

    def __str__(self) -> str:
        return self.URL


class metadata(models.Model):
    Name = models.CharField(max_length=70)
    Version = models.CharField(max_length=70)
    ID = models.CharField(max_length=70, primary_key=True, default='')

    def __str__(self) -> str:
        return self.ID


class Package(models.Model):
    metadata = models.OneToOneField(metadata, on_delete=models.CASCADE, default=None)
    data = models.OneToOneField(data, on_delete=models.CASCADE)

    def __str__(self) -> str:
        return self.metadata.Name
    

class TokenCounter(models.Model):
    
    token_id = models.IntegerField()
    token_count = models.IntegerField(default=0)
    token_hitlimit = models.IntegerField()
    
    def __str__(self):
        return self.token_id
