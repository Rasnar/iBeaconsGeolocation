clear all
close all

% Read all files
beacon1 = dlmread('./captures_finales/Trajet1/David/Thu Dec 11 17:09:11 CET 2014/beacon_b9407f30-f5f8-466e-aff9-25556b57fe6d_11111_1.csv', ';');
beacon2 = dlmread('./captures_finales/Trajet1/David/Thu Dec 11 17:09:11 CET 2014/beacon_b9407f30-f5f8-466e-aff9-25556b57fe6d_11111_2.csv', ';');
beacon3 = dlmread('./captures_finales/Trajet1/David/Thu Dec 11 17:09:11 CET 2014/beacon_b9407f30-f5f8-466e-aff9-25556b57fe6d_11111_3.csv', ';');
beacon4 = dlmread('./captures_finales/Trajet1/David/Thu Dec 11 17:09:11 CET 2014/beacon_b9407f30-f5f8-466e-aff9-25556b57fe6d_11111_4.csv', ';');

% Plot pre filter
figure
hold on
title('Mesures de distance via le telephone numero 1')
xlabel('Temps [s]')
ylabel('Distance [m]')
plot(beacon1(:,3)/1000,beacon1(:,4), 'bx') % plot distances
plot(beacon2(:,3)/1000,beacon2(:,4), 'r+') % plot distances
plot(beacon3(:,3)/1000,beacon3(:,4), 'go') % plot distances
plot(beacon4(:,3)/1000,beacon4(:,4), 'm*') % plot distances
hold off

% Window's size
w_size = 20

for i = 1 : size(beacon1, 1)-w_size
    beacon1(i,4) = median(beacon1([i:i+w_size],4));
end
for i = 1 : size(beacon2, 1)-w_size
    beacon2(i,4) = median(beacon2([i:i+w_size],4));
end
for i = 1 : size(beacon3, 1)-w_size
    beacon3(i,4) = median(beacon3([i:i+w_size],4));
end
for i = 1 : size(beacon4, 1)-w_size
    beacon4(i,4) = median(beacon4([i:i+w_size],4));
end

% Plot post filter
figure
hold on
title('Mesures de distance via le telephone numero 1')
xlabel('Temps [s]')
ylabel('Distance [m]')
plot(beacon1(1:length(beacon1)-w_size,3)/1000,beacon1(1:length(beacon1)-w_size,4), 'bx') % plot distances
plot(beacon2(1:length(beacon1)-w_size,3)/1000,beacon2(1:length(beacon1)-w_size,4), 'r+') % plot distances
plot(beacon3(1:length(beacon1)-w_size,3)/1000,beacon3(1:length(beacon1)-w_size,4), 'go') % plot distances
plot(beacon4(1:length(beacon1)-w_size,3)/1000,beacon4(1:length(beacon1)-w_size,4), 'm*') % plot distances
hold off




